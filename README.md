# World Cup Prediction Game

A web app where users predict World Cup match outcomes and compete on a
leaderboard. This repository is being built in incremental phases.

**Stack**

- Frontend — Angular 18 (standalone components, signals)
- Backend — Java 17 + Spring Boot 3
- Database — PostgreSQL
- ORM — Spring Data JPA / Hibernate
- Auth — JWT (added in a later phase)

---

## Phase 1 — Project Foundation

This phase establishes the technical base of the application. It delivers:

- An Angular frontend project with a `core / shared / features / layout`
  module layout.
- A Spring Boot backend project with package skeletons for `auth`, `user`,
  `match`, `prediction`, `question`, `leaderboard`, `admin`, and `common`.
- PostgreSQL connectivity via Spring Data JPA.
- A working CORS configuration so the Angular dev server can call the API.
- Environment-based configuration on both sides (`environment.ts` /
  `application-{profile}.yml`).
- A `/api/health` endpoint that the frontend calls on startup to confirm the
  full stack is wired.

---

## Repository layout

```
.
├── backend/                 Spring Boot service
│   ├── pom.xml
│   └── src/main/java/com/example/worldcup
│       ├── WorldcupApplication.java
│       ├── auth/            (placeholder — Phase 2)
│       ├── user/            (placeholder)
│       ├── match/           (placeholder)
│       ├── prediction/      (placeholder)
│       ├── question/        (placeholder)
│       ├── leaderboard/     (placeholder)
│       ├── admin/           (placeholder)
│       └── common/          CorsConfig, HealthController
└── frontend/                Angular 18 app
    └── src/app
        ├── core/            singleton services & models (HealthService)
        ├── shared/          reusable UI building blocks
        ├── features/        lazy-loaded feature areas
        └── layout/          app shell / chrome
```

---

## Prerequisites

| Tool          | Version used while building this phase |
| ------------- | -------------------------------------- |
| Node.js       | 20+ (tested with 24)                   |
| npm           | 10+                                    |
| Angular CLI   | 18                                     |
| JDK           | 17                                     |
| Maven         | 3.9+                                   |
| PostgreSQL    | 14+                                    |

---

## 1. Database setup

Create a local PostgreSQL database and user that match the defaults in
[backend/.env.example](backend/.env.example):

```sql
CREATE USER worldcup WITH PASSWORD 'worldcup';
CREATE DATABASE worldcup OWNER worldcup;
GRANT ALL PRIVILEGES ON DATABASE worldcup TO worldcup;
```

You can override any of these via environment variables (see
[backend/src/main/resources/application.yml](backend/src/main/resources/application.yml)):

- `DB_URL` — JDBC URL (default `jdbc:postgresql://localhost:5432/worldcup`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JPA_DDL_AUTO` — `update` in dev, `validate` in prod
- `SPRING_PROFILES_ACTIVE` — `dev` (default) or `prod`
- `CORS_ALLOWED_ORIGINS` — comma-separated, default `http://localhost:4200`

---

## 2. Backend — run locally

```bash
cd backend
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. Verify with:

```bash
curl http://localhost:8080/api/health
# {"status":"UP","service":"worldcup-backend","timestamp":"..."}
```

The Spring Actuator endpoint `http://localhost:8080/actuator/health` is also
exposed.

### Build / test commands

```bash
mvn compile          # compile only
mvn test             # run tests
mvn spring-boot:run  # start the API
mvn package          # produce target/worldcup-0.0.1-SNAPSHOT.jar
```

---

## 3. Frontend — run locally

```bash
cd frontend
npm install   # first time only
npm start     # ng serve on http://localhost:4200
```

Open `http://localhost:4200`. The page displays a **Backend health** card; if
the API is reachable it shows `UP — worldcup-backend` along with the server
timestamp. If the backend is down it shows `DOWN — <error>`.

The API base URL is configured in
[frontend/src/environments/environment.ts](frontend/src/environments/environment.ts).

### Build / test commands

```bash
npm start            # ng serve
npm run build        # production build → dist/frontend
npm test             # karma + jasmine unit tests
```

---

## Phase 1 completion criteria

- [x] Angular app starts locally (`npm start` on port 4200).
- [x] Spring Boot app starts locally (`mvn spring-boot:run` on port 8080).
- [x] Backend connects to PostgreSQL (configured via `application.yml`).
- [x] Angular calls the backend health endpoint and renders the response.

Next up: **Phase 2 — Authentication** (JWT, user registration, login).
