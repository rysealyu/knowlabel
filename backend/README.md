# KnowLabel Backend Documentation

The KnowLabel backend is a Spring Boot service responsible for authenticating with Infisical, communicating with the upstream INCI API via `WebClient`, enforcing security rules, and caching ingredient and formulation analyses in memory using Caffeine. It can be run locally with Maven or as a Docker container.

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Prerequisites & Local Setup](#prerequisites--local-setup)
3. [Configuration & Secrets Management](#configuration--secrets-management)
4. [API Reference](#api-reference)
   - [POST /api/ingredients/analyze](#1-analyze-ingredient-list)
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
├── Dockerfile                                 # Container image definition for the backend
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

`docker-compose.yml` lives at the repository root, outside `backend/`. It builds the image from `backend/Dockerfile` (see [Running with Docker](#running-with-docker)).

---

## Prerequisites & Local Setup

The backend is a Spring Boot app located in `backend/`.

### Prerequisites

- Java 21 (JDK)
- Maven, or use the included `./mvnw` wrapper (no separate install needed)
- Docker Desktop or Docker Engine with Compose v2 (optional, only to run the backend in a container)
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

### Running with Docker

The repo includes `backend/Dockerfile` and a `docker-compose.yml` at the repository root that build and run the backend as a container, so you don't need Java or Maven installed to run it.

**Prerequisites**

- Docker Desktop, or Docker Engine with Compose v2

**Commands** (run from the repository root, not from `backend/`)

```bash
# Build the image and start the backend
docker compose up --build

# Or run it in the background
docker compose up -d --build

# Follow the backend logs
docker compose logs -f backend

# Stop and remove the container
docker compose down
```

The backend is available at `http://localhost:8080`, the same as a local run. Stop any local `./mvnw spring-boot:run` first, because both use port 8080.

**Compose settings**

| Setting          | Value                       | Meaning                                                    |
| ---------------- | --------------------------- | ---------------------------------------------------------- |
| `container_name` | `knowlabel-backend`         | Name of the running container                              |
| `build`          | `./backend` (`Dockerfile`)  | Image is built from `backend/Dockerfile`                   |
| `ports`          | `8080:8080`                 | Host port 8080 maps to container port 8080                 |
| `restart`        | `unless-stopped`            | Container restarts automatically unless you stop it        |
| `environment`    | `SPRING_PROFILES_ACTIVE=dev`| Runs Spring Boot with the `dev` profile                    |

**Credentials in the container**

The container needs the same Infisical credentials as a local run (see [Setting up `application.properties`](#setting-up-applicationproperties)). The Dockerfile copies the entire backend build context into the builder stage, packages resources into the JAR, and copies only that JAR into the runtime image. Because `backend/.dockerignore` does not currently exclude `application.properties`, a local file with real credentials can be copied into the build context and packaged into the image.

Do not build or push an image that has a real `application.properties` baked into it. Anyone with the image can read the credentials. Add `application.properties` to `backend/.dockerignore` before using Docker with local credentials; it is already excluded from version control by the repository `.gitignore`.

**Rebuilding**

Re-run `docker compose up --build` after changing code or `pom.xml`. For a completely clean rebuild, run `docker compose build --no-cache backend`.

### Adding a new dependency

Add the `<dependency>` block to `pom.xml`, then re-run:

```bash
./mvnw clean install
```

### Key `pom.xml` dependencies

| Dependency                       | Purpose                                         |
| -------------------------------- | ----------------------------------------------- |
| `spring-boot-starter-web`        | REST endpoints, embedded server                 |
| `spring-boot-starter-webflux`    | `WebClient` for calling the INCI API            |
| `spring-boot-starter-security`   | Spring Security filter chain                    |
| `spring-boot-starter-cache`      | Spring cache abstraction                        |
| `caffeine`                       | In-memory cache implementation                  |
| `java-dotenv`                    | Dotenv support                                   |
| Infisical Java SDK               | Fetching secrets at startup                     |
| `postgresql`                     | PostgreSQL JDBC driver (not currently used)     |
| `spring-boot-devtools`           | Optional development-time tooling               |
| `spring-boot-starter-json`       | JSON support                                     |
| `commons-collections4`           | Collection utilities used by the service        |

Test-only dependencies include `spring-boot-starter-test` and `spring-boot-starter-webmvc-test`. The application does not currently configure a database or persist data; the PostgreSQL driver is present but unused. Ingredient data is cached in memory (see [Caching Architecture](#caching-architecture)).

---

## Configuration & Secrets Management

The backend never stores API keys in the repository. Secrets are fetched from [Infisical](https://infisical.com/) at startup and injected into the `WebClient` used for the INCI API.

**Flow**

1. `InfisicalConfig` authenticates with Infisical using the credentials in your local `application.properties`.
2. It retrieves the INCI API key from the configured project, environment, and secret path.
3. `InciApiConfig` builds a `WebClient` for the INCI API and attaches the key to outgoing requests.

### Why Infisical instead of storing the key in `application.properties`?

The INCI API key is the only real secret the backend needs. Putting it directly in `application.properties` would be simpler, but it has drawbacks that Infisical avoids:

- **Fewer copies of the key:** a plain-text key in `application.properties` ends up on every contributor's machine, and can be copied into Docker images, logs, and backups. With Infisical, the key stays in one place and is fetched at startup.
- **Easy rotation:** to replace the key, update it once in Infisical. Nothing needs to be re-shared with contributors or rebuilt.
- **Access control:** each contributor gets their own machine identity. Revoking someone's access does not require rotating the INCI key.
- **Environment separation:** `dev`, `staging`, and `prod` can each hold different values without changing code.
- **Lower risk of leaks:** `application.properties` holds only the Infisical machine identity, not the INCI key, so an accidental commit exposes less.

**Trade-offs:** `application.properties` still needs the Infisical credentials, so it must still never be committed, and the backend needs network access to Infisical at startup.

### Setting up `application.properties`

`application.properties.example` is a template that is committed to the repo. Your real `application.properties` holds credentials, lives only on your machine, and must never be committed. (Confirm that `application.properties` is listed in `.gitignore` before your first commit.)

**Step 1: Copy the template**

```bash
cd backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

On Windows PowerShell, use `Copy-Item` with the same two paths.

**Step 2: Fill in the values**

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

```

**Step 3: Get the values (choose one option)**

*Option A: Ask the repo owner (recommended for contributors).* Contact the repository owner ([@rysealyu](https://github.com/rysealyu)) and request access to the project's Infisical workspace, or a machine identity client ID and secret for the `dev` environment. Send the request privately (not in a public issue or PR), and never post the credentials anywhere public. Then paste the values into your `application.properties`.

*Option B: Make your own.* If you don't have access, or you're running your own copy of the project:

1. Create a free account at [infisical.com](https://infisical.com/) and create a new project.
2. Add a secret named `INCI_API_KEY` in the `dev` environment at the `/` path. `InciApiConfig` retrieves exactly that secret using the project ID.
3. Create a machine identity with read access to that project, and note its client ID and client secret.
4. Get an API key from the [INCI API provider](https://inciapi.com/) and store it as the secret from step 2.
5. Copy your project ID and environment slug from the Infisical dashboard.
6. Enter all of these values in `application.properties`.

**Step 4: Verify**

Run `./mvnw spring-boot:run` (or `docker compose up --build` from the repo root). If the app starts on `http://localhost:8080` without an Infisical authentication error, your configuration works. If it fails, see [Troubleshooting](#http-status-codes--error-handling).

**Security rules**

- Never commit `application.properties` or any real credentials.
- `SecurityConfig` disables CSRF and permits every request. It does not configure CORS, so browsers are not explicitly granted access from the Vite dev server origin (`http://localhost:5173`).
- `IngredientController` sanitizes incoming payloads before they reach the service layer.

---

## API Reference

Base URL (local): `http://localhost:8080`

### 1. Analyze Ingredient List

Analyzes a full ingredient list (a product formulation) and returns an analysis for each ingredient plus aggregate results.

**`POST /api/ingredients/analyze`**

**Request body** (`application/json`)

```json
["Aqua", "Glycerin", "Niacinamide", "Phenoxyethanol"]
```

The body is a required JSON array of ingredient-name strings in label order. Null entries are ignored; remaining values are trimmed, uppercased, and sorted before processing.

**Response** `200 OK` (`IngredientAnalysisModel`)

```json
{
  "analysis": {
    "barcode": "string",
    "rawInci": ["string"],
    "parsedIngredients": [
      {
        "inciName": "GLYCERIN",
        "safetyScore": 0,
        "safetyLevel": "string",
        "isAllergen": false,
        "allergenTypes": [],
        "comedogenicityRating": 0,
        "irritancyPotential": "string",
        "pregnancySafe": true,
        "found": true,
        "hasData": true
      }
    ],
    "overallSafetyScore": 0,
    "safetyLevel": "string",
    "allergenFlags": [],
    "skinTypeCompatibility": {},
    "pregnancySafe": true,
    "pregnancyUnsafeIngredients": [],
    "cleanBeautyScore": 0,
    "comedogenicityScore": 0,
    "pfasIngredients": [],
    "coverage": 0,
    "flags": {},
    "analyzedAt": "string"
  }
}
```

See [Data Models](#data-models) for field definitions.

**Behavior**

- The controller removes null entries, trims each remaining value, uppercases the values, sorts them, and builds a comma-separated cache key.
- The service sends the normalized list to the INCI API and stores parsed ingredient results in the ingredient cache.
- Spring Cache may serve repeated analysis requests from the formulation cache.

**Example**

```bash
curl -X POST http://localhost:8080/api/ingredients/analyze \
  -H "Content-Type: application/json" \
  -d '{"ingredients": ["Aqua", "Glycerin", "Niacinamide"]}'
```

## HTTP Status Codes & Error Handling

The controller returns the upstream status for `WebClientResponseException` and returns `500 Internal Server Error` for other exceptions. Spring MVC may return `400 Bad Request` before the controller when the request body cannot be deserialized as a `List<String>`.

| Status                      | When it occurs                                                        |
| --------------------------- | --------------------------------------------------------------------- |
| `200 OK`                    | Analysis request succeeds, including a cache hit.                    |
| Upstream status              | The INCI API returns an HTTP error response.                          |
| `400 Bad Request`           | Request JSON cannot be deserialized as a list of strings.             |
| `500 Internal Server Error` | An unexpected exception occurs, including startup configuration error.|

**Troubleshooting**

- **Application fails to start:** check Infisical credentials and project ID in `application.properties`. The INCI secret is read from the `dev` environment.
- **Every upstream call fails:** confirm the `INCI_API_KEY` secret exists at `/` in the `dev` environment of the configured Infisical project.
- **Container exits right after starting:** run `docker compose logs backend`. A missing or invalid Infisical credential is the most common cause (see [Credentials in the container](#running-with-docker)).
- **Port 8080 already in use:** stop any local `./mvnw spring-boot:run` process or other container using the port, then start again.
- **Changes not showing in Docker:** the image is not rebuilt automatically. Re-run `docker compose up --build`.
- **Stale results:** the cache is in-memory only; restart the service to clear it.

---

## Data Models

Both models are Java records in `com.knowlabel.model`.

### `IngredientModel`

Represents a single ingredient.

| Field                    | Type             | Description                          |
| ------------------------ | ---------------- | ------------------------------------ |
| `inciName`               | `String`         | Standardized INCI name               |
| `safetyScore`             | `int`            | Ingredient safety score              |
| `safetyLevel`             | `String`         | Ingredient safety level              |
| `isAllergen`              | `boolean`        | Whether the ingredient is an allergen |
| `allergenTypes`           | `List<String>`   | Associated allergen types            |
| `comedogenicityRating`    | `Integer`        | Comedogenicity rating                |
| `irritancyPotential`      | `String`         | Irritancy potential                  |
| `pregnancySafe`           | `Boolean`        | Pregnancy-safety indicator            |
| `found`                   | `boolean`        | Whether the ingredient was found      |
| `hasData`                 | `boolean`        | Whether ingredient data is available  |

### `IngredientAnalysisModel`

Response record for bulk analysis of a formulation.

| Field      | Type                                      | Description                   |
| ---------- | ----------------------------------------- | ----------------------------- |
| `analysis` | `IngredientAnalysisModel.AnalysisDetails` | Analysis result details       |

`AnalysisDetails` contains `barcode`, `rawInci`, `parsedIngredients`, `overallSafetyScore`, `safetyLevel`, `allergenFlags`, `skinTypeCompatibility`, `pregnancySafe`, `pregnancyUnsafeIngredients`, `cleanBeautyScore`, `comedogenicityScore`, `pfasIngredients`, `coverage`, `flags`, and `analyzedAt`.

---

## Caching Architecture

Caching uses [Caffeine](https://github.com/ben-manes/caffeine) through Spring's cache abstraction. Caches are defined in `CacheConfig` and are held in memory in the JVM.

| Cache         | Key                                      | Value                     | Maximum size | Expiry       |
| ------------- | ---------------------------------------- | ------------------------- | ------------ | ------------ |
| `ingredients` | Uppercase INCI name                      | `IngredientModel`         | 10,000       | 7 days       |
| `ingredientsAnalysis` | Sorted, uppercase comma-separated list | `IngredientAnalysisModel` | 5,000        | 24 hours     |

Both caches use Caffeine `expireAfterWrite` and record cache statistics. The formulation cache is populated by `POST /api/ingredients/analyze`; the ingredient cache is populated while an analysis response contains parsed ingredients.

**Cache harvesting:** when a formulation is analyzed, `IngredientService` also stores each parsed ingredient result in the `ingredients` cache. No individual ingredient HTTP endpoint is currently exposed.

**Limitations**

- Caches are per instance and are lost on restart.
- Running multiple instances means each keeps its own cache.
- If a shared cache is needed later, replace Caffeine with a distributed option such as Redis.

---

## Contributing

Backend changes follow the repository's [CONTRIBUTING.md](../CONTRIBUTING.md). In short:

- **Commits:** [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/), e.g. `feat: add ingredient safety scoring endpoint` or `build(backend): add spring-boot-starter-validation dependency`.
- **Branches:** never commit directly to `main`. Branch from `main` as `type/short-description` (e.g. `feat/ingredient-scoring`), open a PR into `main`, wait for required checks (CI, CodeQL) to pass, and prefer squash-merging.
- **Dependencies:** edit `pom.xml`, then run `./mvnw clean install`.
- **Docker changes:** edits to `Dockerfile` or `docker-compose.yml` use the `build` type, e.g. `build(backend): add Dockerfile`. Verify them with `docker compose up --build` before opening a PR.

---

## External Resources

- [Spring Boot documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Caffeine](https://github.com/ben-manes/caffeine)
- [Infisical documentation](https://infisical.com/docs)
- [Docker documentation](https://docs.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- [Repository CONTRIBUTING.md](https://github.com/rysealyu/knowlabel/blob/main/CONTRIBUTING.md)