# ⚽ Kickoff — Premier League Card Collecting Game

A production-grade microservices backend built with Java 21, Spring Boot 4, Apache Kafka, PostgreSQL, and Redis.
Deployed on AWS with a fully automated CI/CD pipeline.

> **Live API:** `http://18.196.247.84:8080`

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Game Mechanics](#game-mechanics)
- [Services](#services)
- [Kafka Event Flow](#kafka-event-flow)
- [Testing](#testing)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [AWS Infrastructure](#aws-infrastructure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Future Improvements](#future-improvements)

---

## Overview

Kickoff is a Premier League fantasy card game where players:

- **Predict match scores** to earn coins
- **Open card packs** to collect player cards from all 20 Premier League teams
- **Upgrade cards** from Bronze → Silver → Gold using duplicates
- **Trade cards** with other players via direct 1-for-1 offers
- **Build a Fantasy XI** from their card collection and compete on a leaderboard

Built as a portfolio project to demonstrate modern Java backend engineering — microservice architecture, event-driven
design, and cloud deployment.

---

## Architecture

```
                          ┌─────────────────┐
                          │  Client/Postman │
                          └────────┬────────┘
                                   │ HTTP
                          ┌────────▼────────┐
                          │   API Gateway   │ :8080
                          │   JWT Auth      │
                          └────────┬────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
┌─────────▼──────┐    ┌────────────▼───────┐    ┌──────────▼──────┐
│  User Service  │    │  Match Service     │    │ Prediction Svc  │
│     :8084      │    │      :8081         │    │     :8082       │
│  JWT issuance  │    │  PL data + squads  │    │ Score + evaluate│
└────────────────┘    └────────────────────┘    └─────────────────┘
                                   │ Kafka: match.results
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
┌─────────▼──────┐    ┌────────────▼───────┐    ┌──────────▼──────┐
│  Coin Service  │    │  Card Service      │    │ Fantasy Service │
│     :8083      │    │      :8085         │    │     :8088       │
│  Wallet + txns │    │ Generation + trade │    │ XI + leaderboard│
└────────────────┘    └────────────────────┘    └─────────────────┘
          │                        │
┌─────────▼──────┐    ┌────────────▼───────┐    ┌─────────────────┐
│  Pack Service  │    │  Trade Service     │    │Notification Svc │
│     :8086      │    │      :8087         │    │     :8089       │
│  Pack catalogue│    │  Atomic card swap  │    │  In-app alerts  │
└────────────────┘    └────────────────────┘    └─────────────────┘

                    ┌──────────┐  ┌──────────┐  ┌──────────┐
                    │  Kafka   │  │PostgreSQL│  │  Redis   │
                    │  KRaft   │  │  (RDS)   │  │(ElastiCa)│
                    └──────────┘  └──────────┘  └──────────┘
```

### Key Design Decisions

- **Shared PostgreSQL database, per-service schemas** — simpler local development without sacrificing logical isolation
- **Kafka for async communication** — services are decoupled; match results fan out to predictions, fantasy, and pack
  services simultaneously
- **REST for synchronous operations** — coin deduction before pack opening requires immediate confirmation
- **Redis for caching** — player data cached for 30 days (football-data.org rate limits), fantasy leaderboard as sorted
  set
- **Optimistic locking** — `@Version` on wallet entity prevents race conditions during concurrent coin awards
- **Weighted random without replacement** — card generation uses a mutable pool to guarantee unique players per pack

---

## Tech Stack

| Category         | Technology                       |
|------------------|----------------------------------|
| Language         | Java 21                          |
| Framework        | Spring Boot 4.0.5                |
| Messaging        | Apache Kafka (Confluent KRaft)   |
| Database         | PostgreSQL 16                    |
| Cache            | Redis 7                          |
| ORM              | Hibernate 6 + Spring Data JPA    |
| Migrations       | Flyway                           |
| Mapping          | MapStruct                        |
| Auth             | Spring Security + JJWT 0.12.6    |
| API Docs         | Springdoc OpenAPI                |
| Containerization | Docker + Docker Compose          |
| Cloud            | AWS (EC2, RDS, ElastiCache)      |
| CI/CD            | GitHub Actions + GHCR            |
| Testing          | JUnit 5, Mockito, Testcontainers |
| Data Source      | football-data.org API            |

### Java 21 Features Used

- **Virtual Threads** — prediction-service uses virtual threads for parallel user evaluation across gameweek results
- **Pattern Matching for switch** — api-gateway exception handler uses Java 21 switch expressions
- **Records** — DTOs and events implemented as records throughout

---

## Game Mechanics

### Coins

| Action                        | Coins |
|-------------------------------|-------|
| Correct result prediction     | +5    |
| Correct score prediction      | +25   |
| Sell Bronze card              | +1    |
| Sell Silver card              | +3    |
| Sell Gold card                | +7    |
| Duplicate Gold card from pack | +7    |

### Card Packs

| Pack          | Cards | Cost      | Weekly Limit |
|---------------|-------|-----------|--------------|
| Scout Pack    | 3     | 5 coins   | Unlimited    |
| Transfer Pack | 5     | 30 coins  | 3/week       |
| Golden Pack   | 3     | 150 coins | 1/week       |
| Gameweek Pack | 4     | 80 coins  | 1/week       |

### Card Tiers

- **Bronze** → **Silver**: 2 Bronze cards of same player
- **Silver** → **Gold**: 2 Silver cards of same player

### Card Generation — Weighted Random Without Replacement

300 players across 20 Premier League teams. Cards are generated using a weighted random algorithm that guarantees no
duplicate players within a single pack:

```
weight = teamRarityWeight × playerRarityWeight
```

- Top-6 clubs: `teamRarityWeight = 60` (rarer)
- Other clubs: `teamRarityWeight = 100`
- Star players: `playerRarityWeight = 30`
- Key players: `playerRarityWeight = 60`
- Squad players: `playerRarityWeight = 100`

Lower weight = rarer card. Each selection removes the picked player from the pool, ensuring uniqueness per pack.

---

## Services

### match-service `:8081`

Fetches Premier League fixtures and results from football-data.org. Seeds 300 player squads and 380-game calendar.
Publishes `match.results` events to Kafka after each gameweek fetch.

### prediction-service `:8082`

Accepts match predictions before kickoff (validated against match kickoff time). Consumes `match.results` and evaluates
each user's prediction using Virtual Threads for parallel processing — awarding coins via Kafka for correct results and
scores.

### coin-service `:8083`

Manages user coin wallets with optimistic locking (`@Version`) to prevent race conditions. Uses `@Retryable` with
exponential backoff to handle `ObjectOptimisticLockingFailureException`. Maintains a full transaction log. Consumes
`coins.award` events.

### user-service `:8084`

Handles registration and login. Issues JWT tokens signed with HMAC-SHA512. All other services trust tokens validated by
api-gateway.

### card-service `:8085`

Core game service. Consumes `pack.opened` events and generates cards using weighted random selection without
replacement.
Fetches player data from match-service via REST with 30-day Redis cache. Handles card upgrades and sells.

### pack-service `:8086`

Manages pack catalogue and purchase flow. Enforces weekly limits. Calls coin-service synchronously to deduct coins
before publishing `pack.opened` to Kafka.

### trade-service `:8087`

Enables 1-for-1 card trading between users. Validates card ownership, performs atomic swap via two card transfers.
Trades expire after 48 hours via scheduled job.

### fantasy-service `:8088`

Users select 11 cards as their Fantasy XI per gameweek. Scored after each gameweek based on card tier. Leaderboard
maintained as Redis sorted set (`ZADD`/`ZREVRANGE`).

### notification-service `:8089`

Consumes `trade.status-changed` and `coins.award` events. Stores in-app notifications with Redis unread count cache.

### api-gateway `:8080`

Spring Cloud Gateway (WebFlux). Validates JWT on every request, injects `X-User-Id` and `X-Username` headers for
downstream services. Public paths: `/register`, `/login`.

---

## Kafka Event Flow

```
┌──────────────┐   match.results    ┌──────────────────┐
│match-service │──────────────────► │prediction-service│
└──────────────┘         │          └────────┬─────────┘
                         │                   │ coins.award
                         ▼                   ▼
                  ┌─────────────┐    ┌───────────────┐
                  │fantasy-serv │    │  coin-service │◄─────────────┐
                  └─────────────┘    └───────────────┘              │
                         │                                          │
                         ▼                   ┌──────────────────┐   │
                  ┌─────────────┐            │notification-serv │   │
                  │ pack-service│            └──────────────────┘   │
                  └──────┬──────┘                    ▲              │
                         │ pack.opened               │              │
                         ▼               trade.status-changed       │
                  ┌─────────────┐    ┌──────────────────┐           │
                  │card-service │    │  trade-service   │           │
                  └──────┬──────┘    └──────────────────┘           │
                         │ coins.award                              │
                         └──────────────────────────────────────────┘
```

**Topics:**
| Topic | Producer | Consumers |
|---|---|---|
| `match.results` | match-service | prediction-service, fantasy-service, pack-service |
| `coins.award` | prediction-service, card-service | coin-service, notification-service |
| `pack.opened` | pack-service | card-service |
| `trade.status-changed` | trade-service | notification-service |

All Kafka consumers use `@RetryableTopic` with fixed delay (5s), 2 attempts, and `.DLT` suffix for dead letter topics.

---

## Testing

The project includes unit tests for core business logic and Testcontainers integration tests for services that interact
with PostgreSQL, Kafka, and Redis. Integration tests run automatically in the CI/CD pipeline on every push to `main`.

## Getting Started

### Prerequisites

- Java 21
- Docker + Docker Compose
- IntelliJ IDEA (recommended)

### Environment Variables

Each service requires these environment variables (set in IntelliJ Run Configurations):

```bash
# All services
JWT_SECRET=your-secret-key-minimum-32-characters

# match-service only
FOOTBALL_API_KEY=your-football-data-org-api-key
```

### Run Locally

**1. Start infrastructure:**

```bash
docker compose up -d
```

This starts PostgreSQL, Redis, Kafka (KRaft mode), and Kafka UI.

**2. Start services** (in IntelliJ or via Maven):

```bash
# Start in this order to avoid connection errors
./mvnw spring-boot:run -pl match-service
./mvnw spring-boot:run -pl user-service
./mvnw spring-boot:run -pl coin-service
./mvnw spring-boot:run -pl card-service
./mvnw spring-boot:run -pl pack-service
./mvnw spring-boot:run -pl prediction-service
./mvnw spring-boot:run -pl trade-service
./mvnw spring-boot:run -pl fantasy-service
./mvnw spring-boot:run -pl notification-service
./mvnw spring-boot:run -pl api-gateway
```

**3. Seed match data:**

```bash
curl -X POST http://localhost:8081/api/v1/squads/seed
curl -X POST http://localhost:8081/api/v1/calendar/seed
```

**4. Access tools:**

- Kafka UI: http://localhost:8090
- Swagger (per service): http://localhost:8081/swagger-ui.html

---

## API Reference

All requests go through the API Gateway on port `8080`. Protected endpoints require `Authorization: Bearer <token>`.

### Authentication

**Register:**

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "fanto", "email": "fanto@example.com", "password": "password123"}'
```

**Login:**

```bash
curl -X POST http://localhost:8080/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"username": "fanto", "password": "password123"}'
```

### Game Flow

**Make a prediction:**

```bash
curl -X POST http://localhost:8080/api/v1/predictions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "gameExternalId": 537786,
    "gameweek": 1,
    "predictedHomeScore": 2,
    "predictedAwayScore": 1
  }'
```

**Fetch match results (triggers coin awards):**

```bash
curl -X POST http://localhost:8080/api/v1/games/fetch/1 \
  -H "Authorization: Bearer <token>"
```

**Check coin balance:**

```bash
curl http://localhost:8080/api/v1/coins/balance \
  -H "Authorization: Bearer <token>"
```

**Buy a pack:**

```bash
curl -X POST "http://localhost:8080/api/v1/packs/SCOUT_PACK/buy" \
  -H "Authorization: Bearer <token>"
```

**View card collection:**

```bash
curl http://localhost:8080/api/v1/cards/collection \
  -H "Authorization: Bearer <token>"
```

**Create a trade offer:**

```bash
curl -X POST http://localhost:8080/api/v1/trades/offer \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": "<otherUserId>",
    "offeredCardId": "<cardId>",
    "requestedCardId": "<otherCardId>"
  }'
```

**Submit Fantasy XI:**

```bash
curl -X POST http://localhost:8080/api/v1/fantasy/team \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "gameweek": 1,
    "playerCardIds": ["<cardId1>", "...", "<cardId11>"]
  }'
```

---

## AWS Infrastructure

```
                         Internet
                             │
                             ▼
┌────────────────────────────────────────────────────┐
│                    EC2 t3.small                    │
│                                                    │
│  ┌─────────────┐  ┌───────────┐  ┌─────────────┐   │
│  │ api-gateway │  │   kafka   │  │  9 Spring   │   │
│  │    :8080    │  │   :9092   │  │  services   │   │
│  └─────────────┘  └───────────┘  └─────────────┘   │
└───────────────────┬───────────────────┬────────────┘
                    │                   │
        ┌───────────▼──────────┐  ┌─────▼──────────────┐
        │    RDS PostgreSQL    │  │  ElastiCache Redis │
        │     db.t3.micro      │  │      t3.micro      │
        └──────────────────────┘  └────────────────────┘
```

- **EC2 t3.small** — all 10 services + Kafka in Docker containers
- **RDS db.t3.micro** — PostgreSQL 16 with per-service schemas
- **ElastiCache t3.micro** — Redis 7 for player cache and leaderboards
- **Elastic IP** — static public IP `18.196.247.84`
- **Security Groups** — EC2 accessible on :8080, RDS/Redis accessible only from EC2
- **AWS Secrets Manager** — DB password, JWT secret, API key stored securely
- **IAM Role** — EC2 instance role with Secrets Manager and CloudWatch permissions
- **CloudWatch** — structured JSON logs, 3-day retention, per-service log streams

---

## CI/CD Pipeline

Every push to `main` triggers a full test, build, and deploy:

```
Push to main
    │
    ▼
┌─────────────────────────────────────┐
│  10 parallel test jobs              │
│  (unit tests + Testcontainers ITs)  │
│  fail-fast: false                   │
└─────────────────┬───────────────────┘
                  │ all pass
                  ▼
┌─────────────────────────────────────┐
│  10 parallel Docker builds          │
│  (Maven inside multi-stage build)   │
│  → Push images to GHCR              │
└─────────────────┬───────────────────┘
                  │ all succeed
                  ▼
┌─────────────────────────────────────┐
│  SSH into EC2                       │
│  → Fetch secrets from AWS Secrets   │
│    Manager                          │
│  → Pull new images from GHCR        │
│  → Gradual startup (60s intervals)  │
└─────────────────────────────────────┘
```

**Secrets management:** Sensitive values (DB password, JWT secret, API key) stored in AWS Secrets Manager. Fetched at
deploy time — never stored in git or docker-compose.

---

## Future Improvements

- **Milestone detection** — Full team collection → Golden Pack voucher
- **Gameweek Pack** — Guaranteed player from top-scoring team
- **Fantasy scoring** — Player-level stats when paid API tier available
- **Role-based access control** — Admin endpoints, 403 responses
- **Token revocation** — Redis blacklist for logout
- **Rate limiting** — Redis RequestRateLimiter in api-gateway
- **Healthcheck-based deployment** — Replace sleep intervals with Docker healthchecks
- **AWS ECR** — Replace GHCR with ECR + IAM roles
- **Frontend** — React + Vite + Tailwind
- **HTTPS** — Nginx + Let's Encrypt + DuckDNS

---

## Author

**Yevhenii Koshovets** — Java Developer

GitHub: [@ykoshovets](https://github.com/ykoshovets)