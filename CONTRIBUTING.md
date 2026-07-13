# Contributing guide

This document covers how we work in this repo: commit message format, branch workflow, and how to set up the frontend (npm) and backend (Maven) projects locally.

## Table of contents

- [Commit message format](#commit-message-format)
- [Branch workflow](#branch-workflow)
- [Setting up the backend (Maven)](#setting-up-the-backend-maven)
- [Setting up the frontend (npm)](#setting-up-the-frontend-npm)

---

## Commit message format

We follow [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/). Every commit message should be structured as:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Use for |
|---|---|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only changes |
| `style` | Formatting, missing semicolons, etc. (no code logic change) |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `perf` | A code change that improves performance |
| `test` | Adding or correcting tests |
| `build` | Changes to build tools or dependencies (e.g. `pom.xml`, `package.json`) |
| `ci` | Changes to CI/CD configuration (e.g. GitHub Actions workflows) |
| `chore` | Other changes that don't modify src or test files |

### Examples

```
feat: add ingredient safety scoring endpoint
fix(auth): resolve token expiry not being checked
docs: update setup instructions in README
refactor(service): extract ingredient parsing into its own class
chore: remove unused imports
build(backend): add spring-boot-starter-validation dependency
ci: add Semgrep scanning workflow
```

---

## Branch workflow

1. Never commit directly to `main` — it's protected and requires a pull request.
2. Create a branch from `main`, named by type/short-description:
   ```
   feature/ingredient-scoring
   fix/login-token-expiry
   chore/update-dependencies
   ```
3. Commit using the format above.
4. Push your branch and open a PR into `main`.
5. Wait for required checks (CI, CodeQL) to pass before merging.
6. Prefer squash-merging to keep `main`'s history clean.

---

## Setting up the backend (Maven)

The backend is a Spring Boot app located in `backend/`.

### Prerequisites

- Java 21 (JDK)
- Maven (or use the included `./mvnw` wrapper — no separate install needed)

### First-time setup

```bash
cd backend
./mvnw clean install
```

This downloads all dependencies listed in `pom.xml` and compiles the project.

### Running locally

```bash
./mvnw spring-boot:run
```

Starts the backend on `http://localhost:8080`.

### Key `pom.xml` dependencies (for reference)

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | REST endpoints, embedded server |
| `spring-boot-starter-security` | Endpoint authentication |
| `spring-boot-starter-data-jpa` | ORM / database access |
| `postgresql` | Postgres JDBC driver |

### Adding a new dependency

Add the `<dependency>` block to `pom.xml`, then re-run:

```bash
./mvnw clean install
```

---

## Setting up the frontend (npm)

The frontend is a React + Vite app located in `frontend/`.

### Prerequisites

- Node.js (LTS version)
- npm (comes with Node)

### First-time setup

```bash
cd frontend
npm install
```

This reads `package.json` and downloads all dependencies into `node_modules/` (gitignored).

### Running locally

```bash
npm run dev
```

Starts the dev server, typically on `http://localhost:5173`, with hot-reload.

### Building for production

```bash
npm run build
```

Outputs a static, optimized build to `dist/`.

### Key packages (for reference)

| Package | Purpose |
|---|---|
| `react-router-dom` | Page routing |
| `axios` | API calls to the backend |
| `react-helmet-async` | Per-page metadata |

### Adding a new package

```bash
npm install <package-name>
```

This updates both `package.json` and `package-lock.json` — commit both.

---
