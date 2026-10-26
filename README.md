# java-restassured-junit-testing

**Java + REST Assured + JUnit** — Demo Users API.

[English](README.md) · [Español](README.es.md)

Independent automation example. Each repository owns its tests, dependencies and CI.

## Setup

Python 3.12 is used by the launcher and local API. Node projects use Node 22; Java projects use JDK 21 and Maven 3.9+.

```sh
python -m pip install -r requirements-service.txt
```

## Run

```sh
python scripts/run.py --local-api -- mvn -B verify
```

For an external API, set DEMO_BASE_URL and DEMO_API_KEY and omit --local-api. PARABANK_URL overrides the public UI URL. Environment variables must be set in your shell; .env files are not loaded automatically.

## Structure and reports

Tests describe business scenarios; pages/helpers encapsulate browser and HTTP interactions. scripts/run.py manages the real local API and redacts its ephemeral key from console and text reports. Native reports are retained by Actions even when tests fail.

Read [scenarios](docs/scenarios.md), [validation status](VALIDATION.md) and [CI](.github/workflows/ci.yml).

The public ParaBank instance can fail independently of the tests. No production service or credentials are used. Do not commit credentials or enable API request traces containing authentication headers.
