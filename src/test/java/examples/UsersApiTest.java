package examples;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class UsersApiTest {
    final List<String> created=new ArrayList<>();
    String base;
    RequestSpecification api() { return RestAssured.given().baseUri(base).header("X-API-Key",System.getenv("DEMO_API_KEY")).contentType("application/json"); }
    @BeforeEach void configure() { base=Objects.requireNonNull(System.getenv("DEMO_BASE_URL"),"Use scripts/run.py --local-api"); Objects.requireNonNull(System.getenv("DEMO_API_KEY")); }
    @AfterEach void cleanup() { for(String id:created) { int status=api().delete("/users/"+id).statusCode(); assertTrue(status==204 || status==404); } }
    Map<String,Object> create() {
        Map<String,Object> payload=Map.of("name","Automation Example","email",UUID.randomUUID()+"@example.com","role","sales","active",true);
        Response response=api().body(payload).post("/users"); assertEquals(201,response.statusCode());
        String id=response.jsonPath().getString("id"); created.add(id); assertDoesNotThrow(()->UUID.fromString(id));
        assertEquals("/users/"+id,response.header("Location"));
        payload.forEach((k,v)->assertEquals(v,response.jsonPath().get(k)));
        return response.jsonPath().getMap("");
    }
    @Test void crud() {
        var user=create(); String path="/users/"+user.get("id");
        assertEquals(user,api().get(path).jsonPath().getMap(""));
        Response patch=api().body(Map.of("active",false)).patch(path); assertEquals(200,patch.statusCode()); assertFalse(patch.jsonPath().getBoolean("active"));
        String email=UUID.randomUUID()+"@example.com";
        Response put=api().body(Map.of("name","Replaced","email",email)).put(path); assertEquals(200,put.statusCode());
        assertEquals("Replaced",put.jsonPath().getString("name")); assertEquals(email,put.jsonPath().getString("email"));
        assertEquals("support",put.jsonPath().getString("role")); assertTrue(put.jsonPath().getBoolean("active"));
        assertEquals(204,api().delete(path).statusCode()); assertEquals(404,api().get(path).statusCode());
    }
    @Test void duplicateEmail() { var user=create(); assertEquals(409,api().body(Map.of("name","Duplicate","email",user.get("email"))).post("/users").statusCode()); }
    @Test void lifecycleAndFiltering() {
        var user=create(); String path="/users/"+user.get("id");
        assertEquals(409,api().post(path+"/activate").statusCode());
        Response deactivate=api().post(path+"/deactivate"); assertEquals(200,deactivate.statusCode()); assertFalse(deactivate.jsonPath().getBoolean("active"));
        assertEquals(409,api().post(path+"/deactivate").statusCode());
        Response list=api().queryParam("role","sales").queryParam("active",false).queryParam("limit",100).get("/users");
        assertEquals(200,list.statusCode()); assertTrue(list.jsonPath().getList("items.id").contains(user.get("id")));
        assertEquals(200,api().post(path+"/activate").statusCode());
    }
    @Test void invalidPayloads() {
        for(var data:List.of(Map.of("name","","email","bad"),Map.of("name","A","email","a@example.com","role","owner"),Map.of("name","A","email","a@example.com","unknown",true)))
            assertEquals(422,api().body(data).post("/users").statusCode());
    }
    @Test void invalidPatchAndPagination() { var user=create(); assertEquals(422,api().body("{\"name\":null}").patch("/users/"+user.get("id")).statusCode()); assertEquals(422,api().get("/users?limit=0").statusCode()); }
    @Test void authenticationAndNotFound() {
        Response health=RestAssured.given().baseUri(base).get("/health"); assertEquals(200,health.statusCode()); assertEquals("ok",health.jsonPath().getString("status"));
        assertEquals(401,RestAssured.given().baseUri(base).get("/users").statusCode());
        assertEquals(404,api().get("/users/"+UUID.randomUUID()).statusCode());
    }
}
