# java-restassured-junit-testing

**Java + REST Assured + JUnit** — Demo Users API.

[English](README.md) · [Español](README.es.md)

Ejemplo independiente: este repositorio contiene sus propias pruebas, dependencias y CI.

## Instalación

El lanzador y la API local usan Python 3.12. Los proyectos Node usan Node 22; los de Java, JDK 21 y Maven 3.9+.

```sh
python -m pip install -r requirements-service.txt
```

## Ejecución

```sh
python scripts/run.py --local-api -- mvn -B verify
```

Para una API externa, define DEMO_BASE_URL y DEMO_API_KEY y omite --local-api. PARABANK_URL cambia la URL web. Define las variables en tu terminal: los archivos .env no se cargan automáticamente.

## Estructura y reportes

Las pruebas describen escenarios; pages/helpers encapsulan las interacciones web y HTTP. scripts/run.py administra la API local real y oculta su credencial temporal en consola y reportes de texto. Actions conserva los reportes nativos incluso si fallan las pruebas.

Consulta [scenarios](docs/scenarios.md), [validation status](VALIDATION.md) and [CI](.github/workflows/ci.yml).

ParaBank público puede fallar independientemente de las pruebas. Usa solamente datos ficticios. No subas credenciales ni actives trazas de API que guarden encabezados de autenticación.
