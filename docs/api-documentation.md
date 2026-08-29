# GameForge-Live API Reference

All endpoints accept and return JSON. Authenticated endpoints require `Authorization: Bearer <JWT>`.

---

## 1. Authentication (`/api/auth`)

### `POST /api/auth/register`
Registers a new player account and returns an auth token.
```json
{
  "username": "Bhavashesh",
  "email": "bhavashesh@gameforge.live",
  "password": "Password@123"
}
```

### `POST /api/auth/login`
Authenticates player credentials.
```json
{
  "username": "Bhavashesh",
  "password": "Password@123"
}
```

---

## 2. Player Progression (`/api/players`)

### `GET /api/players/me` *(Bearer)*
Returns authenticated player stats, XP, level, and wallet balances.

### `POST /api/players/me/add-xp` *(Bearer)*
Awards XP with active LiveOps event multiplier applied.
```json
{
  "baseAmount": 500,
  "source": "RACE_WIN"
}
```

---

## 3. Leaderboards (`/api/leaderboards`)

### `POST /api/leaderboards/scores` *(Bearer)*
Submits match score to global / seasonal leaderboard.
```json
{
  "leaderboardName": "GLOBAL",
  "score": 15500.0,
  "gameMode": "RACING"
}
```

### `GET /api/leaderboards/global?leaderboardName=GLOBAL&limit=10`
Returns Top 10 players globally.

### `GET /api/leaderboards/player/{playerId}/rank?leaderboardName=GLOBAL`
Returns player rank, top percentile, and surrounding competitive bracket.

---

## 4. LiveOps & Live Events (`/api/liveops`)

### `GET /api/liveops/events/active`
Returns currently active multiplier events (Double XP, Coin Frenzy) for game HUD.

### `POST /api/liveops/events` *(Admin)*
Schedules a new LiveOps event.

---

## 5. Achievement Engine (`/api/achievements`)

### `GET /api/achievements`
List all game achievements and reward payouts.

### `POST /api/achievements/process-event` *(Bearer)*
Triggers achievement evaluation from telemetry.
```json
{
  "metric": "RACES_WON",
  "incrementBy": 1
}
```

---

## 6. Feature Flags (`/api/features`)

### `GET /api/features/evaluate/player/{playerId}`
Evaluates all feature flags and rollout assignments for a player.

### `PUT /api/features/{id}/rollout?percentage=50` *(Admin)*
Adjusts gradual rollout percentage.

---

## 7. Analytics & Dashboard (`/api/analytics`)

### `POST /api/analytics/events` *(Bearer)*
Ingests client telemetry events (`GAME_STARTED`, `GAME_COMPLETED`, `LEVEL_FAILED`).

### `GET /api/analytics/dashboard`
Returns live platform KPIs: DAU, total events, popular game modes, average session duration.

---

## 8. A/B Testing (`/api/experiments`)

### `GET /api/experiments/player/{playerId}/variants`
Returns assigned variant buckets and custom JSON configuration.
