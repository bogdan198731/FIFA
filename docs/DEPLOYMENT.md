# Production deployment guide

End-to-end instructions for shipping the World Cup Prediction Game to:

- **Backend** (Spring Boot + PostgreSQL) → **Render**
- **Frontend** (Angular) → **Cloudflare Pages**

Both providers have a free tier; this guide also flags the practical limits
(cold starts, DB expiration) you'll hit there.

```
┌─────────────────────┐  HTTPS  ┌───────────────────────────┐  JDBC  ┌────────────────────┐
│  Cloudflare Pages   │ ──────► │  Render Web Service       │ ─────► │  Render PostgreSQL │
│  (Angular static)   │         │  Spring Boot (Java 17)    │        │  16                │
└─────────────────────┘         └───────────────────────────┘        └────────────────────┘
```

---

## 0. Before you start

- [ ] Code is on GitHub on a branch you're happy to deploy from (e.g. `main`).
- [ ] You have:
  - a Render account ([render.com](https://render.com))
  - a Cloudflare account ([dash.cloudflare.com](https://dash.cloudflare.com))
  - a secret manager (1Password, Bitwarden, etc.) to hold the JWT secret you
    generate below.
- [ ] Generate a real JWT secret (≥ 32 bytes). Locally:

  ```bash
  # On any Unix-ish shell or Git Bash:
  openssl rand -base64 48
  ```

  Save it; you'll paste it into Render in §1.3.

---

## 1. Backend → Render

Render gives you three primitives we need: a managed Postgres instance, a
"Web Service" that builds and runs your JAR, and environment variables that
get injected at boot. We provision the database first so the web service has
something to connect to.

### 1.1 Create the PostgreSQL database

1. Render Dashboard → **New +** → **PostgreSQL**.
2. **Name**: `worldcup-db` (anything works — this is just the Render label).
3. **Region**: pick the one closest to your users. Remember it — the web
   service must live in the **same region** for the free internal network to
   work.
4. **PostgreSQL version**: 16.
5. **Plan**: Free is fine for a tournament. ⚠ Free Postgres expires after
   90 days; upgrade or back up before then.
6. Click **Create database**, then on the database's page note these values
   (you'll need them in §1.3):
   - **Hostname** (internal, e.g. `dpg-xxxxx-a`)
   - **Port** (5432)
   - **Database**
   - **Username**
   - **Password** (reveal it once and store it)

### 1.2 Create the Web Service

1. Render Dashboard → **New +** → **Web Service**.
2. Connect your GitHub repo, pick the branch you want to deploy (e.g.
   `main`).
3. **Name**: `worldcup-api`.
4. **Region**: same as the database above.
5. **Runtime**: **Docker** is not needed — pick **Native Runtime → Java**.
6. **Root Directory**: `backend`
7. **Build Command**:

   ```
   ./mvnw clean package -DskipTests
   ```

   If the wrapper isn't committed, use `mvn clean package -DskipTests`. Tests
   are skipped because they don't need a DB but the build takes long enough
   already on a free CPU.

8. **Start Command**:

   ```
   java -Dserver.port=$PORT -jar target/worldcup-0.0.1-SNAPSHOT.jar
   ```

   Render injects `$PORT` (typically `10000`). We pass it explicitly because
   Spring Boot's `application.yml` reads `SERVER_PORT`, not `PORT`.

9. **Plan**: Free works. ⚠ Free Web Services **sleep after 15 min of
   inactivity** — the first request after sleep takes 30-60 s. Move to
   Starter ($7/month) if that's not acceptable.

10. **Health Check Path**: `/api/health` (Render uses this to confirm the
    service started and to keep it healthy).

Don't click **Create Web Service** yet — fill in the environment variables
first.

### 1.3 Environment variables

In the same form, scroll to **Environment** → **Add Environment Variable**
and add each of these:

| Key                     | Value                                                                       |
| ----------------------- | --------------------------------------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`| `prod`                                                                      |
| `DB_URL`                | `jdbc:postgresql://<HOST>:<PORT>/<DBNAME>` (from §1.1)                       |
| `DB_USERNAME`           | from §1.1                                                                   |
| `DB_PASSWORD`           | from §1.1                                                                   |
| `JPA_DDL_AUTO`          | `validate`                                                                  |
| `JPA_SHOW_SQL`          | `false`                                                                     |
| `JWT_SECRET`            | the value you generated in §0                                               |
| `JWT_EXPIRATION_MS`     | `86400000` (24 h — optional, this is also the default)                      |
| `CORS_ALLOWED_ORIGINS`  | placeholder for now, e.g. `https://example.com` — we update this in §3      |

Notes:

- The `DB_URL` **must** start with `jdbc:postgresql://`. Render shows you a
  raw `postgresql://user:pass@host/db` URL; strip the credentials and add the
  `jdbc:` prefix — credentials go in their own variables.
- Use the **internal** hostname (the one without `.render.com`) so traffic
  stays inside Render's network and doesn't count toward egress.

Click **Create Web Service**. Render starts the first build immediately.

### 1.4 First boot

The first build takes 3-5 minutes on a free instance (Maven warm-up). Watch
the **Logs** tab. Look for:

```
Started WorldcupApplication in X seconds
Tomcat started on port(s): 10000 (http)
```

Flyway runs on startup, applying `V1__init_schema.sql` through `V5__...` in
order. If a migration fails, the app refuses to start — fix it locally and
push again rather than editing the DB by hand.

### 1.5 Verify

Render gives you a URL like `https://worldcup-api.onrender.com`. Test:

```bash
curl https://worldcup-api.onrender.com/api/health
# {"status":"UP","service":"worldcup-backend","timestamp":"..."}

curl https://worldcup-api.onrender.com/api/leaderboard
# []  ← empty until users sign up and predictions are scored
```

If `/api/health` returns 200 you're done with the backend. Note the full URL
— the frontend needs it.

---

## 2. Frontend → Cloudflare Pages

Cloudflare Pages serves the built Angular bundle as a global static site. No
server-side rendering, no Node runtime — the app makes HTTPS calls to your
Render backend directly.

### 2.1 Bake the production API URL into the frontend

`frontend/src/environments/environment.prod.ts` currently reads:

```ts
export const environment = {
  production: true,
  apiBaseUrl: '/api'
};
```

Update it to your Render URL **before** pushing:

```ts
export const environment = {
  production: true,
  apiBaseUrl: 'https://worldcup-api.onrender.com/api'
};
```

Commit and push that change to the branch you'll deploy.

> 💡 If you'd rather not commit the URL, use Cloudflare's build environment
> variables in §2.3 and read `import.meta.env` or fold them in via a
> pre-build script. For one app, the in-repo value is simpler.

### 2.2 Tell Cloudflare where the assets live (`wrangler.jsonc`)

Cloudflare has folded the classic "Pages" experience into the unified
**Workers Builds + Static Assets** flow. Deploys now run
`wrangler versions upload`, which fails if it can't find a wrangler config
declaring the asset directory — the symptom is a build log ending in
`If you are uploading a directory of assets ...` followed by `Failed: error
occurred while running deploy command`.

Commit this file at `frontend/wrangler.jsonc`:

```jsonc
{
  "$schema": "node_modules/wrangler/config-schema.json",
  "name": "worldcup-pool",
  "compatibility_date": "2025-01-01",
  "assets": {
    "directory": "./dist/frontend/browser",
    "not_found_handling": "single-page-application"
  }
}
```

Three things to know:

- **`name`** must match the project name you picked in the Cloudflare
  dashboard. If yours isn't `worldcup-pool`, change it.
- **`assets.directory`** is relative to this file. Angular 18 with the
  application builder emits to `dist/frontend/browser/` by default — that's
  what's reflected here.
- **`assets.not_found_handling: "single-page-application"`** is the
  Workers-Assets equivalent of the old Pages `_redirects` file: any
  unmatched route serves `index.html` so the Angular router can take over.
  No separate `_redirects` file is needed.

Verify locally:

```bash
cd frontend
npx ng build --configuration=production
ls dist/frontend/browser/index.html     # should exist
```

Commit + push.

### 2.3 Create the project in Cloudflare

The dashboard flow is the same whether you create a classic Pages project
or a new Workers Builds project — Cloudflare nudges everything onto the
unified runtime now.

1. Cloudflare Dashboard → **Workers & Pages** → **Create** →
   **Connect to Git**.
2. Pick the repo, then the branch (`main` or whatever you're deploying).
3. **Project name**: must match the `name` you set in `wrangler.jsonc`
   (e.g. `worldcup-pool`). This becomes the subdomain
   `worldcup-pool.pages.dev`.
4. **Framework preset**: `Angular`.
5. **Root directory**: `frontend`
6. **Build command**:

   ```
   npx ng build --configuration=production
   ```

7. **Build output directory**: leave blank or set to `dist/frontend/browser`.
   With `wrangler.jsonc` committed, the value in `assets.directory` wins,
   so the dashboard setting is a safety net rather than the source of
   truth.

8. **Environment variables** → click **Add variable**:

   | Key            | Value  |
   | -------------- | ------ |
   | `NODE_VERSION` | `20`   |

9. Click **Save and Deploy**.

The first build downloads `node_modules` from scratch (1-2 min) and produces
the bundle (~30 s). When it's done, Cloudflare gives you a URL like
`https://worldcup-pool.pages.dev`.

### 2.4 (Optional) Custom domain

Pages → your project → **Custom domains** → **Set up a custom domain**.
Cloudflare handles the TLS cert. If the domain isn't already on Cloudflare,
follow their prompts to point its nameservers; otherwise it's instant.

---

## 3. Tie them together (CORS)

Right now your backend only allows the placeholder origin from §1.3, so
requests from Cloudflare get blocked at the preflight.

1. Render → `worldcup-api` → **Environment** → edit `CORS_ALLOWED_ORIGINS`:

   ```
   https://worldcup-pool.pages.dev,https://your-custom-domain.com
   ```

   (Comma-separated, no spaces, no trailing slash. Include both the
   `*.pages.dev` URL and any custom domain.)

2. Render auto-restarts the service with the new env. Wait ~30 s for the
   health check to go green.

3. End-to-end smoke test in the browser:
   - Open the Cloudflare URL.
   - Sign in with a fresh account (or seed an admin via psql).
   - Navigate to `/dashboard` and `/leaderboard` — both should load without
     console CORS errors.

If you see `Access-Control-Allow-Origin` errors in DevTools, the Cloudflare
URL almost certainly doesn't match `CORS_ALLOWED_ORIGINS` byte-for-byte
(trailing `/`, http vs https, www).

---

## 4. Day-2 operations

### Logs

- **Render**: Service → **Logs** tab. Tail in real time, filter by severity.
- **Cloudflare**: Pages → project → **Deployments** → click a deployment →
  **View build log**. Runtime logs aren't available for static Pages — use
  browser DevTools for client errors.

### Re-deploying

- **Backend**: push to the tracked branch. Render auto-deploys.
- **Frontend**: same — push and Cloudflare rebuilds. Builds are immutable
  per commit, so previewing PRs is automatic if you enable "Preview
  deployments" in the Pages project settings.

### Rolling back

- **Render** → **Events** → previous deploy → **Rollback to this deploy**.
- **Cloudflare** → **Deployments** → previous deploy → **Retry deployment**
  promotes it back to production.

### Rotating the JWT secret

The JWT secret signs every session token. Rotating it invalidates **every
logged-in user** — they'll be bounced to `/login`.

1. Generate a new value (`openssl rand -base64 48`).
2. Render → env vars → update `JWT_SECRET`.
3. Render auto-restarts. Users get a 401 on their next request; the
   frontend's auth interceptor logs them out automatically.

### Backing up the database

Render Postgres → **Backups** → **Take backup**. Free tier retains backups
for 7 days. For the tournament, take one before opening registration and
one before announcing final results.

---

## 5. Troubleshooting

| Symptom                                           | Cause                                                      | Fix                                                                                                                              |
| ------------------------------------------------- | ---------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| First request after a quiet period takes ~45 s    | Free Web Service slept                                     | Either accept it, upgrade to Starter, or hit `/api/health` from an uptime monitor every 10 min.                                  |
| Browser console: `CORS policy: No 'Access-Control...'` | `CORS_ALLOWED_ORIGINS` doesn't match the actual page URL  | Copy the exact origin from the address bar (scheme + host, no path, no trailing `/`) into the env var on Render.                 |
| `/leaderboard` direct-loaded → 404                | SPA fallback not configured                                | Confirm `assets.not_found_handling: "single-page-application"` is in `frontend/wrangler.jsonc` and a fresh deploy has shipped.    |
| Build log ends in `If you are uploading a directory of assets ...` | No wrangler config; Cloudflare can't find the bundle      | Commit `frontend/wrangler.jsonc` as in §2.2. Make sure the `name` matches your Cloudflare project name byte-for-byte.            |
| `npm error EUSAGE ... package-lock.json ... out of sync`           | Lockfile drift between local npm version and Cloudflare's | Delete `node_modules` + `package-lock.json` locally, reinstall with the same npm major Cloudflare uses (npm 10 with Node 20), and commit the regenerated lockfile. |
| Backend logs `IllegalStateException: app.jwt.secret is not configured` | `JWT_SECRET` env var missing                              | Set it on Render → service restarts.                                                                                             |
| Flyway error `Validate failed: Migrations have failed validation` | Someone edited a tracked migration's contents             | Don't edit applied migrations; add a new `V6__...sql` to fix forward.                                                            |
| `401 Unauthorized` on every authed call after a redeploy | JWT secret changed                                         | Expected — sign in again to mint a token under the new secret.                                                                   |
| Render build OOM / very slow                      | Free CPU + Maven warm-up                                   | Add `MAVEN_OPTS=-Xmx512m` env var; or upgrade to Starter for ~3× the build speed.                                                |

---

## 6. (Optional) Infrastructure as code

If you'd rather provision Render from a file in the repo, commit this as
`render.yaml` at the repo root and pick **Blueprint** instead of **Web
Service** when creating:

```yaml
databases:
  - name: worldcup-db
    plan: free
    region: frankfurt          # ← match to web service
    postgresMajorVersion: 16

services:
  - type: web
    name: worldcup-api
    runtime: java
    region: frankfurt
    plan: free
    rootDir: backend
    buildCommand: ./mvnw clean package -DskipTests
    startCommand: java -Dserver.port=$PORT -jar target/worldcup-0.0.1-SNAPSHOT.jar
    healthCheckPath: /api/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: JPA_DDL_AUTO
        value: validate
      - key: JPA_SHOW_SQL
        value: "false"
      - key: JWT_EXPIRATION_MS
        value: "86400000"
      - key: JWT_SECRET
        generateValue: true     # Render generates a strong value once
      - key: CORS_ALLOWED_ORIGINS
        value: https://worldcup-pool.pages.dev
      - key: DB_URL
        fromDatabase:
          name: worldcup-db
          property: connectionString   # ← prefix with jdbc: in code, see below
      - key: DB_USERNAME
        fromDatabase:
          name: worldcup-db
          property: user
      - key: DB_PASSWORD
        fromDatabase:
          name: worldcup-db
          property: password
```

⚠ Render's `connectionString` property returns the raw `postgresql://...`
URL, not a JDBC one. Either:

- swap the start command to assemble it (`-Dspring.datasource.url=jdbc:$DB_URL`), or
- set `DB_URL` manually in the dashboard after the first deploy.

Cloudflare Pages has no equivalent file — its setup lives in the dashboard
or via the `wrangler pages` CLI if you prefer scripted config.
