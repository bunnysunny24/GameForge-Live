# GameForge-Live Architecture & System Design

## 1. System Overview
**GameForge-Live** is a backend services and LiveOps platform engineered for high-concurrency mobile and online games. It delivers the core systems needed to operate a game beyond the game engine client:
- Identity & Progression Management
- High-Throughput Global Leaderboards
- Dynamic LiveOps & Event Scheduling
- Rule-Based Achievement Engine
- Feature Flagging with Deterministic Percentage Rollouts
- Telemetry Ingestion & Real-Time Analytics
- A/B Testing & Live Experimentation

```
                   🎮 GAME CLIENT / MOBILE ENGINE
                                 │
                   (REST / JSON / JWT Authentication)
                                 ▼
         ┌───────────────────────────────────────────────┐
         │            Spring Boot 3 API Layer            │
         └───────┬───────────┬───────────┬───────────────┘
                 │           │           │
     ┌───────────┴┐     ┌────┴─────┐   ┌─┴─────────────┐
     │ Players &  │     │ LiveOps  │   │ Feature Flags │
     │ Security   │     │ Engine   │   │ & Rollouts    │
     └─────┬──────┘     └────┬─────┘   └─┬─────────────┘
           │                 │           │
     ┌─────┴──────┐     ┌────┴─────┐   ┌─┴─────────────┐
     │ Leaderboard│     │ Achieve- │   │ Analytics &   │
     │ (ZSET/JPA) │     │ ments    │   │ Telemetry     │
     └─────┬──────┘     └────┬─────┘   └─┬─────────────┘
           │                 │           │
           ▼                 ▼           ▼
     ┌─────────────────────────────────────────────────┐
     │   PostgreSQL (Persistent) + Redis (ZSET Cache)  │
     └─────────────────────────────────────────────────┘
```

---

## 2. Key Architecture Pillars

### A. High-Performance Leaderboards ($O(\log N)$ Ranking)
- **Redis Sorted Sets (`ZSET`)**: Player scores are stored as elements in a sorted set using `ZADD`.
- **Top-N Queries**: Executed via `ZREVRANGEBYSCORE` / `ZREVRANGE` in $O(\log N + M)$ time.
- **Rank Computation**: Real-time rank determined via `ZREVRANK` in $O(\log N)$ time.
- **Graceful Fallback**: If Redis is offline or disabled, the system seamlessly queries indexed PostgreSQL tables (`ORDER BY score DESC, updated_at ASC`).

### B. Dynamic LiveOps Engine
- Events are scheduled with ISO-8601 timestamps (`startTime`, `endTime`) and multiplier modifiers.
- When players earn XP or coins, the engine computes active multipliers in real-time ($O(1)$ lookup).
- Zero-downtime event toggles for emergency live ops control.

### C. Rule-Based Achievement Engine
- Evaluates telemetry metrics (`RACES_WON`, `GAMES_PLAYED`, `TOTAL_SCORE`, `LEVEL_REACHED`).
- Atomic progress tracking in `player_achievements`.
- Automatically grants multi-currency rewards (Coins, XP, Gems) upon condition fulfillment.

### D. Feature Flagging & Deterministic Rollouts
- Controlled feature release without persistent state drift.
- Bucketing formula:
  $$\text{bucket} = \text{CRC32}(\text{flagKey} + \text{":"} + \text{playerId}) \pmod{100}$$
- If $\text{bucket} < \text{rolloutPercentage}$, feature is enabled for that player.
- Deterministic: Player 42 will always be in the same bucket across all servers and sessions.

### E. A/B Experimentation Framework
- Hash-based variant partitioning across arbitrary traffic splits (e.g. 50/50 Control vs Boosted Rewards).
- Dynamic config JSON payload delivery to client engines.
