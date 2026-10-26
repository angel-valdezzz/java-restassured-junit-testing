# Application contracts

UI: ParaBank. Every positive case registers a unique fictional customer.
Never use john/demo, fixed account IDs, implicit test ordering, or real customer data.
UI cases cover registration, logout/login, required fields, password mismatch,
invalid credentials, and opening a savings account when supported by the suite.

API: the separate Demo Users API, pinned by commit in requirements-service.txt.
Authenticated requests use X-API-Key. POST/PUT require name and email.
Roles: admin/support/sales. PATCH null is rejected (422); duplicate email is 409.
Missing/invalid credentials return 401. DELETE returns 204, subsequent GET is 404.
Each API test creates unique users and removes them in cleanup where the runner
supports finally/fixture teardown. Local CI starts a fresh real service.

ParaBank is an external shared service. An outage is a failing dependency, not a skip
or a successful test. Automatic test retries are disabled. Use fresh data on reruns.
Suite source is the authoritative scenario inventory; feature parity differs for
experimental TestScript and is documented in its README.
