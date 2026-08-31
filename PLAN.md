# Spring Redis Demo — Project Plan

## Overview

A Spring Boot 3 application that demonstrates Redis-backed caching (LRU eviction strategy)
with a PostgreSQL persistence layer and a React frontend. The project is fully containerised
via Docker Compose and can be started with a single `./run` command.

---

## Goals

- Show the difference between a cache HIT and a cache MISS in a real HTTP request cycle
- Demonstrate cache eviction triggered by a write operation
- Expose live cache statistics (hit count, miss count, hit rate) via a dedicated API endpoint
- Visualise the above in a minimal React UI that auto-updates every 3 seconds

---

## Tech Stack

| Layer       | Technology                              |
|-------------|----------------------------------------|
| Language    | Java 17                                |
| Framework   | Spring Boot 3.3.x                      |
| Persistence | Spring Data JPA + PostgreSQL 15        |
| Caching     | Spring Cache + Redis 7 (LRU policy)    |
| Redis client| Lettuce (bundled with Spring Data Redis)|
| Build       | Maven 3.x                              |
| Frontend    | React 18 + Vite (served via nginx)     |
| Containers  | Docker + Docker Compose                |
| Testing     | JUnit Jupiter + Mockito + Testcontainers|
| Coverage    | JaCoCo                                 |

---

## Domain

A single table `numbers` in PostgreSQL:

| Column       | Type      | Notes                          |
|--------------|-----------|-------------------------------|
| id           | UUID (PK) | Generated via GenerationType.UUID |
| value        | INTEGER   | The stored integer             |
| created_at   | TIMESTAMP | Auto-populated via @PrePersist |

---

## API Endpoints

| Method | Path               | Description                                   |
|--------|--------------------|-----------------------------------------------|
| POST   | /api/numbers       | Persist a new number; evicts cache             |
| GET    | /api/numbers       | Return all numbers; served from cache on HIT  |
| GET    | /api/cache/stats   | Return hit count, miss count, hit rate        |
| DELETE | /api/cache         | Manually evict the entire numbers cache        |

The GET /api/numbers response carries an `X-Cache-Status: HIT|MISS` response header
so clients can observe cache behaviour without reading server logs.

---

## Caching Design

- Cache name: `numbers`
- Cache key: `'all'` (single entry for the full list)
- `@Cacheable(cacheNames = "numbers", key = "'all'")` on `NumberService.getAllNumbers()`
- `@CacheEvict(cacheNames = "numbers", key = "'all'")` on `NumberService.createNumber()`
- `LoggingCache` wraps every `Cache` instance from `RedisCacheManager` via a
  `LoggingCacheManager` decorator; it overrides `get()` to log HIT/MISS and update
  `CacheStatsService` counters
- `CacheStatsService` also holds a `ThreadLocal<Boolean>` so the controller can read
  the HIT/MISS status for the current request and set the `X-Cache-Status` header

---

## LRU Eviction

Configured at the Redis server level via CLI args in docker-compose.yml:

```
redis-server --maxmemory 100mb --maxmemory-policy allkeys-lru
```

This is the idiomatic Redis approach — no application-level code change needed.

---

## Project Structure

```
spring-redis-demo/
├── PLAN.md
├── pom.xml
├── Dockerfile                        — multi-stage Maven build → JRE 17 runtime
├── docker-compose.yml                — postgres + redis + app + frontend (4 services)
├── run                               — ./run starts the full stack
├── frontend/
│   ├── Dockerfile                    — Vite build → nginx:alpine
│   ├── nginx.conf                    — serves SPA, proxies /api to app:8080
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── api.js
│       └── components/
│           ├── AddNumberForm.jsx
│           ├── NumbersTable.jsx
│           └── CacheStatsPanel.jsx
└── src/
    ├── main/
    │   ├── java/com/purplevarun/springredisdemo/
    │   │   ├── SpringRedisDemoApplication.java
    │   │   ├── config/
    │   │   │   ├── CacheConfig.java      — RedisCacheManager + LoggingCacheManager
    │   │   │   ├── LoggingCache.java     — Cache decorator: logs HIT/MISS
    │   │   │   └── RedisConfig.java      — LettuceConnectionFactory
    │   │   ├── controller/
    │   │   │   ├── CacheController.java  — GET /api/cache/stats, DELETE /api/cache
    │   │   │   └── NumberController.java — POST + GET /api/numbers
    │   │   ├── dto/
    │   │   │   ├── CacheStatsResponse.java
    │   │   │   └── NumberRequest.java
    │   │   ├── model/
    │   │   │   └── NumberEntry.java
    │   │   ├── repository/
    │   │   │   └── NumberRepository.java
    │   │   └── service/
    │   │       ├── CacheStatsService.java
    │   │       └── NumberService.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/purplevarun/springredisdemo/
            ├── controller/
            │   ├── CacheControllerTest.java
            │   └── NumberControllerTest.java
            ├── integration/
            │   └── NumberCacheIntegrationTest.java
            └── service/
                ├── CacheStatsServiceTest.java
                ├── LoggingCacheTest.java
                └── NumberServiceTest.java
```

---

## Docker Compose — Single Command Startup

```bash
./run          # runs: docker compose up --build
```

Service startup order:
1. `postgres` starts → healthcheck (`pg_isready`) passes
2. `redis` starts → healthcheck (`redis-cli ping`) passes
3. `app` (Spring Boot :8080) starts — depends on both passing healthy
4. `frontend` (nginx :3000) starts — depends on app started

Hibernate `ddl-auto: update` auto-creates the `numbers` table on first boot.
No manual SQL or init scripts needed.

---

## Planned Commits

| #  | Prefix    | Message                                                           |
|----|-----------|-------------------------------------------------------------------|
| 1  | [Chore]   | Preparing the plan                                                |
| 2  | [Chore]   | Initialize spring boot project with maven                         |
| 3  | [Chore]   | Add docker-compose with postgresql, redis and run script          |
| 4  | [Feature] | Add number entity with uuid, value and created_at                 |
| 5  | [Feature] | Add number jpa repository                                         |
| 6  | [Feature] | Implement post api to create number entries                       |
| 7  | [Feature] | Implement get api to fetch all numbers                            |
| 8  | [Feature] | Configure redis as spring cache provider with lru eviction        |
| 9  | [Feature] | Add logging cache decorator and cache stats service               |
| 10 | [Feature] | Add cacheable and cacheevict annotations to number service        |
| 11 | [Feature] | Add cache stats endpoint and manual eviction endpoint             |
| 12 | [Chore]   | Add dockerfile for containerised deployment                       |
| 13 | [Test]    | Add unit tests for number service                                 |
| 14 | [Test]    | Add unit tests for number controller                              |
| 15 | [Test]    | Add unit tests for cache stats service, controller and logging cache |
| 16 | [Test]    | Add integration tests with testcontainers for cache behaviour     |
| 17 | [Feature] | Add x-cache-status response header to get numbers endpoint        |
| 18 | [Chore]   | Scaffold react frontend with vite                                 |
| 19 | [Feature] | Add number form and numbers list components                       |
| 20 | [Feature] | Add live cache stats panel with hit/miss visualisation            |
| 21 | [Chore]   | Add frontend dockerfile, nginx config and wire into docker-compose|
