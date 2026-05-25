# One Stop Fantasy

A full-stack web app that connects your fantasy sports accounts and shows all upcoming events — lineup locks, waiver deadlines, drafts — in one unified dashboard.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17 + Spring Boot 3.5 |
| Database | Supabase (PostgreSQL) |
| Auth | Spring Security + JWT |
| Frontend | React 18 + Vite 5 |
| Containerisation | Docker + Docker Compose |

## Project structure

```
one-stop-fantasy/
├── backend/         Spring Boot API (Maven)
├── frontend/        React + Vite app
├── docker-compose.yml
└── .env             Secrets — never committed (see setup below)
```

## Local development setup

### Prerequisites

- Java 17
- Node.js 20
- Docker Desktop (for the Docker workflow)

### 1. Clone and configure secrets

Create a `.env` file at the project root:

```
SUPABASE_PASSWORD=your_supabase_db_password
JWT_SECRET=your_random_32_char_secret
```

Generate a JWT secret with PowerShell:
```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

### 2a. Run with Docker (recommended for a quick spin-up)

```powershell
docker compose up --build
```

- Frontend → http://localhost:5173
- Backend  → http://localhost:8080

To stop: `docker compose down`

> The frontend Docker image is a production build — no hot reload. Use option 2b for active development.

### 2b. Run locally (recommended for active development)

**Backend** — reads secrets from `application-local.properties`:

Create `backend/src/main/resources/application-local.properties` (gitignored):
```properties
SUPABASE_PASSWORD=your_supabase_db_password
JWT_SECRET=your_jwt_secret
```

Then start the backend:
```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

**Frontend:**
```powershell
cd frontend
npm install
npm run dev
```

- Frontend → http://localhost:5173 (hot reload enabled)
- Backend  → http://localhost:8080

## Running tests

```powershell
cd backend
.\mvnw.cmd test
```

Tests use an in-memory H2 database and do not require Supabase credentials.

## Current features

- User registration and login (JWT auth)
- Connect a Sleeper account — automatically fetches all current NFL leagues
- Disconnect leagues
- Leagues persisted to Supabase PostgreSQL

## Supported platforms

| Platform | Status |
|---|---|
| Sleeper | ✅ Implemented |
| ESPN | Planned |
| FPL (Fantasy Premier League) | Planned |
