# Progress Tracker

A one-page habit tracker. Sign up with just an email, verify it once via a
link, then mark each day **done** (green), **tried** (yellow), or **not
done** (red) on a calendar. Counts update live. The signup date is the first
trackable day.

## How it's built (standard Spring layering)

```
entity/       User, DayEntry, DayStatus       — JPA models
repository/   UserRepository, DayEntryRepository — Spring Data JPA
service/      UserService, DayEntryService, EmailService — business logic
controller/   AuthController, CalendarController — REST endpoints
dto/          request/response shapes, kept separate from entities
config/       GlobalExceptionHandler
resources/static/  index.html, css/, js/ — the single page
```

There's deliberately **no Spring Security** here — identity is just "has
this email been verified," matching what you asked for. That's fine for
learning and for a low-stakes personal tool; the "Future hardening" section
below says what you'd add before letting strangers use it.

## Endpoints

| Method | Path                  | Purpose                              |
|--------|-----------------------|---------------------------------------|
| POST   | /api/auth/signup      | Create/re-request verification        |
| GET    | /api/auth/verify?token=  | Clicked from the email link         |
| GET    | /api/auth/status?email=  | Poll whether an email is verified   |
| GET    | /api/calendar?email=  | Fetch all entries + stats             |
| PUT    | /api/calendar/entry   | Upsert a day's status                 |

## Running it locally

You need Java 17+, Maven, and PostgreSQL (or just use Docker Compose below).

1. Create a database: `createdb tracker` (user/pass `tracker`/`tracker`, or edit `application.yml`).
2. Set real mail credentials as env vars (a Gmail **app password** works, or any SMTP relay):
   ```
   export MAIL_USERNAME=you@gmail.com
   export MAIL_PASSWORD=your-app-password
   ```
3. `mvn spring-boot:run`
4. Open http://localhost:8080

### Or with Docker Compose (app + Postgres together)

```
MAIL_USERNAME=you@gmail.com MAIL_PASSWORD=your-app-password docker compose up --build
```

## Deployment plan

**Stage 1 — containerize (done above).** The Dockerfile is a multi-stage
build (Maven build → slim JRE runtime), so the shipped image only carries the
JAR, not the build toolchain.

**Stage 2 — pick a host.** For a learning project, in order of least effort:
1. **Railway or Render** — connect the GitHub repo, they detect the
   Dockerfile, add a managed Postgres plugin, set `MAIL_USERNAME` /
   `MAIL_PASSWORD` / `APP_BASE-URL` as env vars, done. Free/cheap tiers exist
   on both.
2. **Fly.io** — `fly launch` picks up the Dockerfile; `fly postgres create`
   gives you a managed database; secrets via `fly secrets set`.
3. **A single VPS (DigitalOcean/Hetzner droplet)** — run
   `docker compose up -d` there directly; put Caddy or nginx in front for
   free HTTPS via Let's Encrypt.

**Stage 3 — production-shape config.**
- Move `application.yml` secrets fully to environment variables (already
  templated that way for datasource/mail); never commit real credentials.
- Set `spring.jpa.hibernate.ddl-auto` to `validate` and manage schema changes
  with **Flyway** or **Liquibase** instead of `update`, once the schema is stable.
- Use a real transactional email provider (SendGrid, Postmark, AWS SES)
  instead of a personal Gmail account — better deliverability, no daily
  send caps.
- Point `app.base-url` at your real domain so verification links resolve.

**Stage 4 — CI/CD (optional next step).** A simple GitHub Actions workflow:
`mvn test` → build Docker image → push to a registry → trigger a deploy on
Railway/Fly/your VPS. Add this once you're iterating often enough that manual
deploys get annoying.

## Future hardening (if this grows beyond a personal project)

- Replace "email = identity" with real auth (password + Spring Security, or
  magic-link tokens that expire and are single-use — right now a verified
  email never re-proves ownership on later visits).
- Rate-limit `/api/auth/signup` to stop email-bombing.
- Add a delete/clear-day endpoint instead of only DONE/TRIED/NOT_DONE.
- Pagination or date-range limits on `/api/calendar` once entry history gets long.
