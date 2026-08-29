# GameForge-Live System Design & Technical Deep-Dive

## 1. High-Throughput Leaderboards: PostgreSQL vs Redis

### Problem:
In a live game with 1,000,000 active players, querying rank via SQL (`SELECT COUNT(*) FROM scores WHERE score > ?`) requires scanning index nodes for every request, introducing DB bottleneck under high concurrency.

### Solution:
We use a **Dual Storage Model**:
1. **Redis Sorted Sets (`ZSET`)** for $O(\log N)$ real-time score ingestion (`ZADD`) and rank lookup (`ZREVRANK`).
2. **PostgreSQL / JPA** as the durable system of record with periodic reconciliation and offline fallback.

| Metric | SQL DB Only | Redis ZSet + DB |
|---|---|---|
| Rank Lookup Time | 25ms - 200ms | **< 1ms** |
| Top-100 Extraction | 15ms - 50ms | **< 1ms** |
| Score Ingestion Write | Disk I/O Bound | In-Memory Async |

---

## 2. Deterministic Stateless Feature Flag Rollouts

### Problem:
Gradual feature rollout (e.g. 25% of users receive `NEW_RACING_MODE`) typically requires storing persistent flags per player or session in a database, causing storage bloat and cross-node cache invalidation issues.

### Solution:
GameForge-Live uses **Deterministic Hashing**:
```java
public static int computePlayerBucket(String flagKey, Long playerId) {
    String input = flagKey + ":" + playerId;
    CRC32 crc32 = new CRC32();
    crc32.update(input.getBytes(StandardCharsets.UTF_8));
    return (int) (crc32.getValue() % 100);
}
```
- **Zero DB writes** during player evaluation.
- **Consistent experience**: A player assigned to the 25% bucket remains enabled across every device, server, and reconnect until rollout percentage changes.
- **Whitelist override**: Supports direct player ID bypass for developer and VIP accounts.

---

## 3. Dynamic LiveOps Architecture

```
Client Action (Race Finished)
             │
             ▼
   PlayerService.addXp(baseXp)
             │
             ▼
   LiveOpsService.getActiveMultipliers()
        ├── StartTime <= Now <= EndTime?
        └── Returns max multiplier (e.g. 2.0x)
             │
             ▼
   Calculated XP = baseXp * Multiplier
             │
             ▼
   Level Progress & Currency Rewards Updated
```
