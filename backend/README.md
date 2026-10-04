# KnowLabel Backend Documentation

The KnowLabel backend is a Spring Boot service responsible for authenticating with Infisical, communicating with the upstream INCI API via `WebClient`, enforcing security rules, and caching ingredient and formulation analyses in memory using Caffeine.

> Items marked `TODO(verify)` were inferred from the project structure and should be checked against the source before publishing.

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Prerequisites & Local Setup](#prerequisites--local-setup)
3. [Configuration & Secrets Management](#configuration--secrets-management)
4. [API Reference](#api-reference)
   - [POST /api/skincare/analyze](#1-analyze-ingredient-list)
   - [GET /api/skincare/ingredient/{inciName}](#2-get-single-ingredient-details)
5. [HTTP Status Codes & Error Handling](#http-status-codes--error-handling)
6. [Data Models](#data-models)
7. [Caching Architecture](#caching-architecture)
8. [Contributing](#contributing)
9. [External Resources](#external-resources)

---

## Project Structure

```text
backend/
├── .mvn/                                      # Maven Wrapper configuration
├── mvnw / mvnw.cmd                            # Maven Wrapper scripts (Unix / Windows)
├── src/
│   ├── main/
│   │   ├── java/com/knowlabel/
│   │   │   ├── Server.java                    # Application entry point
│   │   │   ├── config/
│   │   │   │   ├── CacheConfig.java           # Caffeine cache definitions
│   │   │   │   ├── InciApiConfig.java         # WebClient & API key injection
│   │   │   │   ├── InfisicalConfig.java       # Infisical SDK authentication
│   │   │   │   └── SecurityConfig.java        # Spring Security filter chain
│   │   │   ├── controller/
│   │   │   │   └── IngredientController.java  # REST API endpoints & payload sanitization
│   │   │   ├── model/
│   │   │   │   ├── IngredientAnalysisModel.java # Bulk analysis response record
│   │   │   │   └── IngredientModel.java       # Individual ingredient record
│   │   │   └── service/
│   │   │       └── IngredientService.java     # Business logic, WebClient calls & cache harvesting
│   │   └── resources/
│   │       └── application.properties.example # Template for local properties
│   └── test/
│       └── java/com/knowlabel/
│           └── AppTest.java                   # Unit and integration tests
└── pom.xml                                    # Maven dependencies and build configuration
```

---

## Prerequisites & Local Setup

The backend is a Spring Boot app located in `backend/`.

### Prerequisites

- Java 21 (JDK)
- Maven, or use the included `./mvnw` wrapper (no separate install needed)
- Access to the project's Infisical workspace and an INCI API key stored there (see [Configuration](#configuration--secrets-management))

### First-time setup

1. Go to the backend folder:

   ```bash
   cd backend
   ```

2. Copy the example properties file to create your own local `application.properties`:

   ```bash
   # macOS / Linux / Git Bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties

   # Windows (PowerShell)
   Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
   ```

3. Open the new `application.properties` and fill in every value. You need either credentials from the repo owner or your own (see [Setting up `application.properties`](#setting-up-applicationproperties)). **The app will not start until this is done.**

4. Build the project:

   ```bash
   ./mvnw clean install
   ```

`./mvnw clean install` downloads all dependencies listed in `pom.xml`, compiles the project, and runs the tests.

### Running locally

```bash
./mvnw spring-boot:run
```

The backend starts on `http://localhost:8080`.

### Running tests

```bash
./mvnw test
```

### Adding a new dependency

Add the `<dependency>` block to `pom.xml`, then re-run:

```bash
./mvnw clean install
```

### Key `pom.xml` dependencies

| Dependency                       | Purpose                                         |
| -------------------------------- | ----------------------------------------------- |
| `spring-boot-starter-web`        | REST endpoints, embedded server                 |
| `spring-boot-starter-security`   | Endpoint authentication and request filtering   |
| `spring-boot-starter-webflux`    | `WebClient` for calling the INCI API            |
| `spring-boot-starter-cache`      | Spring cache abstraction                        |
| `caffeine`                       | In-memory cache implementation                  |
| Infisical Java SDK               | Fetching secrets at startup                     |

`TODO(verify)`: confirm these against `pom.xml`. CONTRIBUTING.md also lists `spring-boot-starter-data-jpa` and `postgresql`. The backend currently uses only in-memory caching, so either those dependencies are unused and should be removed, or they are planned and this section should say so.

---

## Configuration & Secrets Management

The backend never stores API keys in the repository. Secrets are fetched from [Infisical](https://infisical.com/) at startup and injected into the `WebClient` used for the INCI API.

**Flow**

1. `InfisicalConfig` authenticates with Infisical using the credentials in your local `application.properties`.
2. It retrieves the INCI API key from the configured project, environment, and secret path.
3. `InciApiConfig` builds a `WebClient` for the INCI API and attaches the key to outgoing requests.

### Setting up `application.properties`

`application.properties.example` is a template that is committed to the repo. Your real `application.properties` holds credentials, lives only on your machine, and must never be committed. (Confirm that `application.properties` is listed in `.gitignore` before your first commit.)

**Step 1: Copy the template**

```bash
cd backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

On Windows PowerShell, use `Copy-Item` with the same two paths.

**Step 2: Fill in the values**

`TODO(verify)`: replace the keys below with the exact names from `application.properties.example`.

```properties
# ---------------------------------------------------------------
# src/main/resources/application.properties
# Local only. NEVER commit this file or share its values.
# ---------------------------------------------------------------

# --- Infisical authentication ----------------------------------
# Client ID of the Infisical machine identity (from the repo owner, or your own)
infisical.client-id=YOUR_CLIENT_ID

# Client secret of that machine identity. Treat it like a password.
infisical.client-secret=YOUR_CLIENT_SECRET

# --- Infisical secret location ---------------------------------
# ID of the Infisical project that stores the INCI API key
infisical.project-id=YOUR_PROJECT_ID

# Environment slug the secret lives in (e.g. dev, staging, prod)
infisical.environment=dev
```

**Step 3: Get the values (choose one option)**

*Option A: Ask the repo owner (recommended for contributors).* Contact the repository owner ([@rysealyu](https://github.com/rysealyu)) and request access to the project's Infisical workspace, or a machine identity client ID and secret for the `dev` environment. Send the request privately (not in a public issue or PR), and never post the credentials anywhere public. Then paste the values into your `application.properties`.

*Option B: Make your own.* If you don't have access, or you're running your own copy of the project:

1. Create a free account at [infisical.com](https://infisical.com/) and create a new project.
2. Add a secret for the INCI API key in your chosen environment (for example `dev`). The secret name must match the name `InfisicalConfig` / `InciApiConfig` looks up. `TODO(verify)`: add the exact secret name.
3. Create a machine identity with read access to that project, and note its client ID and client secret.
4. Get an API key from your INCI API provider, and store it as the secret from step 2. `TODO(verify)`: link the provider's signup page.
5. Copy your project ID and environment slug from the Infisical dashboard.
6. Enter all of these values in `application.properties`.

**Step 4: Verify**

Run `./mvnw spring-boot:run`. If the app starts on `http://localhost:8080` without an Infisical authentication error, your configuration works. If it fails, see [Troubleshooting](#http-status-codes--error-handling).

**Security rules**

- Never commit `application.properties` or any real credentials.
- `SecurityConfig` defines the Spring Security filter chain for all endpoints. `TODO(verify)`: document CORS settings, in particular whether the Vite dev server origin (`http://localhost:5173`) is allowed.
- `IngredientController` sanitizes incoming payloads before they reach the service layer.

---

## API Reference

Base URL (local): `http://localhost:8080`

### 1. Analyze Ingredient List

Analyzes a full ingredient list (a product formulation) and returns an analysis for each ingredient plus aggregate results.

**`POST /api/skincare/analyze`**

**Request body** (`application/json`) `TODO(verify)`

```json
{
  "ingredients": ["Aqua", "Glycerin", "Niacinamide", "Phenoxyethanol"]
}
```

| Field         | Type       | Required | Description                                       |
| ------------- | ---------- | -------- | ------------------------------------------------- |
| `ingredients` | `string[]` | Yes      | INCI names in label order. Input is sanitized.    |

**Response** `200 OK` (`IngredientAnalysisModel`) `TODO(verify)`

```json
{
  "ingredients": [
    {
      "inciName": "Glycerin"
    }
  ]
}
```

See [Data Models](#data-models) for field definitions.

**Behavior**

- The controller sanitizes and normalizes the list.
- The service checks the cache for the full formulation, then for each individual ingredient, and calls the INCI API only for what is missing.
- Individual ingredient results from a bulk call are stored in the ingredient cache so later lookups are served from memory (see [Caching Architecture](#caching-architecture)).

**Example**

```bash
curl -X POST http://localhost:8080/api/skincare/analyze \
  -H "Content-Type: application/json" \
  -d '{"ingredients": ["Aqua", "Glycerin", "Niacinamide"]}'
```

### 2. Get Single Ingredient Details

Returns details for one ingredient by its INCI name.

**`GET /api/skincare/ingredient/{inciName}`**

| Parameter  | In   | Type     | Description                                          |
| ---------- | ---- | -------- | ---------------------------------------------------- |
| `inciName` | path | `string` | INCI name, e.g. `Niacinamide`. URL-encode spaces.    |

**Response** `200 OK` (`IngredientModel`)

```json
{
  "inciName": "Niacinamide"
}
```

**Example**

```bash
curl http://localhost:8080/api/skincare/ingredient/Niacinamide
```

---

## HTTP Status Codes & Error Handling

`TODO(verify)`: confirm each code against the controller and exception handling.

| Status                      | When it occurs                                                             |
| --------------------------- | -------------------------------------------------------------------------- |
| `200 OK`                    | Request succeeded (including cache hits).                                  |
| `400 Bad Request`           | Malformed JSON, empty list, or input rejected during sanitization.         |
| `401 Unauthorized` / `403 Forbidden` | Request rejected by the Spring Security filter chain.             |
| `404 Not Found`             | Ingredient not found in the INCI API.                                      |
| `429 Too Many Requests`     | Upstream INCI API rate limit reached.                                      |
| `500 Internal Server Error` | Unexpected server error, or Infisical authentication failed at startup.    |
| `502 / 503`                 | Upstream INCI API unreachable or returned a server error.                  |

**Troubleshooting**

- **Application fails to start:** check Infisical credentials, project ID, and environment in `application.properties`.
- **Every upstream call fails:** confirm the INCI API key exists in Infisical under the project and environment set in `application.properties`.
- **Stale results:** the cache is in-memory only; restart the service to clear it.

---

## Data Models

Both models are Java records in `com.knowlabel.model`. `TODO(verify)`: replace the fields below with the actual record components.

### `IngredientModel`

Represents a single ingredient.

| Field      | Type     | Description                  |
| ---------- | -------- | ---------------------------- |
| `inciName` | `String` | Standardized INCI name       |

### `IngredientAnalysisModel`

Response record for bulk analysis of a formulation.

| Field         | Type                    | Description                          |
| ------------- | ----------------------- | ------------------------------------ |
| `ingredients` | `List<IngredientModel>` | Per-ingredient results, label order  |

---

## Caching Architecture

Caching uses [Caffeine](https://github.com/ben-manes/caffeine) through Spring's cache abstraction. Caches are defined in `CacheConfig` and are held in memory in the JVM.

| Cache         | Key                          | Value                     | Populated by                      |
| ------------- | ---------------------------- | ------------------------- | --------------------------------- |
| Ingredient    | Normalized INCI name         | `IngredientModel`         | Single lookups and bulk analysis  |
| Formulation   | Normalized ingredient list   | `IngredientAnalysisModel` | `POST /api/skincare/analyze`      |

`TODO(verify)`: confirm cache names, key format, maximum size, and expiry (TTL) in `CacheConfig`.

**Cache harvesting:** when a formulation is analyzed, `IngredientService` also stores each ingredient result in the ingredient cache. A later `GET /api/skincare/ingredient/{inciName}` for any of those ingredients is then served without an upstream call.

**Limitations**

- Caches are per instance and are lost on restart.
- Running multiple instances means each keeps its own cache.
- If a shared cache is needed later, replace Caffeine with a distributed option such as Redis.

---

## Contributing

Backend changes follow the repository's [CONTRIBUTING.md](../CONTRIBUTING.md). In short:

- **Commits:** [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/), e.g. `feat: add ingredient safety scoring endpoint` or `build(backend): add spring-boot-starter-validation dependency`.
- **Branches:** never commit directly to `main`. Branch from `main` as `type/short-description` (e.g. `feature/ingredient-scoring`), open a PR into `main`, wait for required checks (CI, CodeQL) to pass, and prefer squash-merging.
- **Dependencies:** edit `pom.xml`, then run `./mvnw clean install`.

---

## External Resources

- [Spring Boot documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Caffeine](https://github.com/ben-manes/caffeine)
- [Infisical documentation](https://infisical.com/docs)
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- [Repository CONTRIBUTING.md](https://github.com/rysealyu/knowlabel/blob/main/CONTRIBUTING.md)