# 🌐 GameForge-Live
> **LiveOps, Game Services & Real-Time Backend Platform for Modern Online Games**

[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7.0%20Sorted%20Sets-red.svg)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![OpenAPI](https://img.shields.io/badge/Live%20Swagger%20UI-OpenAPI%203.0-green.svg)](https://gameforge-live.onrender.com/swagger-ui/index.html)
[![Live Demo](https://img.shields.io/badge/Live%20API-Render.com-brightgreen.svg)](https://gameforge-live.onrender.com/api/leaderboards/global)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 🌐 Live Production Deployment

GameForge-Live is deployed and live on cloud infrastructure:

- 📖 **Interactive Swagger UI (API Docs)**: [https://gameforge-live.onrender.com/swagger-ui/index.html](https://gameforge-live.onrender.com/swagger-ui/index.html)
- 🏆 **Global Leaderboard API**: [https://gameforge-live.onrender.com/api/leaderboards/global](https://gameforge-live.onrender.com/api/leaderboards/global)
- 🎁 **Active LiveOps Events API**: [https://gameforge-live.onrender.com/api/liveops/events/active](https://gameforge-live.onrender.com/api/liveops/events/active)
- 📊 **Platform Analytics Dashboard**: [https://gameforge-live.onrender.com/api/analytics/dashboard](https://gameforge-live.onrender.com/api/analytics/dashboard)
- 🎯 **Achievements Catalog**: [https://gameforge-live.onrender.com/api/achievements](https://gameforge-live.onrender.com/api/achievements)

### 🔑 Pre-Seeded Production Accounts
- **Admin**: `admin` / `Admin@123`
- **Player**: `Bhavashesh` / `Password@123`
- **Player**: `CyberKnight` / `Password@123`

---

## 🧠 What is GameForge-Live?

Imagine you have created a successful online/mobile game with thousands or millions of active players. Now you need a resilient, scalable backend platform to power:
- 👤 **Player Identity & Progression** (Level curves, multi-currency wallets, profiles)
- 🏆 **Global & Seasonal Leaderboards** ($O(\log N)$ real-time ranking powered by Redis Sorted Sets)
- 🎁 **LiveOps & Event Engine** (Time-windowed dynamic multipliers: Weekend Double XP, Coin Frenzy)
- 🎯 **Achievement Engine** (Telemetry-driven rule evaluations, automatic badge & reward grants)
- 🚩 **Feature Flags & Controlled Rollouts** (Stateless deterministic hash-bucketing: 10% → 25% → 50% → 100%)
- 📊 **Game Analytics & Telemetry** (High-throughput ingestion, DAU, win rate, session KPIs)
- 🧪 **A/B Experimentation** (Consistent player variant allocation & dynamic config payloads)
- 🔐 **Stateless Security** (JWT Bearer authentication, BCrypt encryption, role-based access)

GameForge-Live is designed specifically to represent the backend systems of modern live-service titles (such as those developed and operated by **EA Slingshot** and EA Mobile).

---

## 🏗️ Architecture

```
                    🎮 GAME CLIENT / REST CONSUMERS
                                   │
                           (REST / JSON / JWT)
                                   ▼
             ┌───────────────────────────────────────────┐
             │       GameForge-Live API Layer            │
             │           (Spring Boot 3)                 │
             └─────────────────────┬─────────────────────┘
                                   │
   ┌─────────────┬───────────┬─────┴─────┬───────────┬─────────────┐
   ▼             ▼           ▼           ▼           ▼             ▼
[Player &     [Global     [LiveOps    [Achieve-   [Feature      [Analytics &
 Security]   Leaderboard]  Events]     ments]      Flags]        Telemetry]
   │             │           │           │           │             │
   │      (Redis ZSET / JPA) │           │    (CRC32 Hashing)      │
   ▼             ▼           ▼           ▼           ▼             ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │               Persistence Layer (PostgreSQL / Redis / H2)       │
 └─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Key Systems Deep-Dive

### 1. 🏆 High-Performance Leaderboards ($O(\log N)$ Ranking)
- **Redis Sorted Sets (`ZSET`)**: Ingests scores via `ZADD`, queries top-N brackets with `ZREVRANGE`, and performs sub-millisecond rank computations via `ZREVRANK`.
- **Hybrid Storage & Graceful Fallback**: Dual-stores scores in PostgreSQL for durability and automatically falls back to indexed SQL ranking if Redis is unavailable.

### 2. 🎁 Dynamic LiveOps & Multiplier Engine
- Manages scheduled live operations events (e.g. *Weekend Double XP*, *Midweek Coin Frenzy*).
- Real-time $O(1)$ multiplier calculation dynamically modulates XP and currency earnings when games finish.

### 3. 🎯 Rule-Based Achievement Engine
- Submits telemetry events (e.g. `RACES_WON`, `GAMES_PLAYED`, `TOTAL_SCORE`, `LEVEL_REACHED`).
- Rule engine matches metrics, updates player progress, unlocks achievements, and distributes coin/XP/gem rewards.

### 4. 🚩 Stateless Feature Flags & Controlled Rollouts
- Replaces database lookups with deterministic CRC32 hash bucketing:
  $$\text{bucket} = \text{CRC32}(\text{flagKey} + \text{":"} + \text{playerId}) \pmod{100}$$
- Ensures reliable, stateless cohort assignment across distributed backend instances with whitelist overrides.

### 5. 📊 Real-Time Analytics & KPIs
- Telemetry buffer tracking player sessions, game completion rates, and popularity breakdown.
- Aggregates platform KPIs (DAU, average playtime, win rates) for LiveOps dashboard monitoring.

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Language & Framework** | Java 17 LTS, Spring Boot 3.2.3 |
| **Data Persistence** | Spring Data JPA, Hibernate, PostgreSQL 16, H2 (In-Memory) |
| **Caching & Fast Ranking** | Redis 7 (Sorted Sets `ZSET`), Spring Data Redis |
| **Security & Identity** | Spring Security 6, JJWT 0.12.5, BCrypt |
| **Documentation** | OpenAPI 3.0, Swagger UI (`/swagger-ui.html`) |
| **DevOps & Containers** | Docker, Docker Compose |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |

---

## ⚡ Quick Start

### Option A: Run Standalone (Zero external dependencies)
In standalone mode, GameForge-Live runs using in-memory H2 database with pre-seeded demo data:

```powershell
# Clone the repository
git clone https://github.com/Bhavashesh/GameForge-Live.git
cd GameForge-Live

# Build and run
.\mvnw.cmd spring-boot:run
```

### Option B: Run with Docker Compose (PostgreSQL 16 + Redis 7)
```powershell
docker-compose up --build
```

---

## 📖 Interactive API Documentation

Once the server is running, explore and test the entire API via Swagger UI:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Pre-Seeded Demo Credentials:
- **Admin Account**: `admin` / `Admin@123` (Role: `ROLE_ADMIN`)
- **Player Account**: `Bhavashesh` / `Password@123` (Role: `ROLE_PLAYER`)
- **Player Account**: `CyberKnight` / `Password@123` (Role: `ROLE_PLAYER`)

---

## 🧪 Testing

Run the full automated test suite:
```powershell
.\mvnw.cmd test
```

---

## 🎮 Interview Architecture Highlights

> "GameForge-Live represents the foundational LiveOps and backend architecture that powers modern online multiplayer games. Built with Java 17 and Spring Boot 3, it tackles real-world distributed game service challenges:
> 1. **Leaderboard Throughput**: Utilizing Redis Sorted Sets to achieve sub-millisecond $O(\log N)$ ranking across millions of active players.
> 2. **LiveOps Dynamics**: Zero-downtime event scheduling with dynamic XP/currency multiplier calculation.
> 3. **Stateless Rollouts**: Deterministic CRC32 hashing for progressive feature delivery without database overhead.
> 4. **Unified Player Service**: Seamless interplay between telemetry, achievement triggers, and wallet transactions."
