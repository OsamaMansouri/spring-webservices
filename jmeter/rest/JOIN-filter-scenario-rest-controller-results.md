# 📊 Résultats - Scénario JOIN-filter (RestController)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~00:56:15
- **Heure fin** : ~01:01:00
- **Threads** : 60
- **Ramp-up** : 60s
- **Durée** : 480s (8 min)
- **Payload** : Néant (lectures uniquement)

## Mix de Requêtes

- Init - GET Items
- 70% : GET /api-rest/items?categoryId={id}
- 30% : GET /api-rest/items/{id}

---

## Résultats JMeter

### Summary Report

| Endpoint              | # Samples  | Average (ms) | Min (ms) | Max (ms)  | Std. Dev.  | Error %   | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| --------------------- | ---------- | ------------ | -------- | --------- | ---------- | --------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items      | 14,421     | 346          | 18       | 4,552     | 284.78     | 0.00%     | 75.5             | 391.69          | 16.88       | 5312.7     |
| GET /items?categor... | 10,071     | 346          | 20       | 5,801     | 293.82     | 0.00%     | 52.7             | 273.67          | 12.57       | 5312.8     |
| GET /items/{id} (30%) | 4,314      | 271          | 1        | 1,507     | 183.03     | 0.00%     | 22.6             | 93.47           | 3.54        | 4227.8     |
| **TOTAL**             | **28,806** | **335**      | **1**    | **5,801** | **276.61** | **0.00%** | **150.8**        | **758.43**      | **32.98**   | **5150.3** |

### Aggregate Report

| Endpoint            | # Samples  | Median (p50) | 90th pct | 95th pct | 99th pct  | Error %   | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| ------------------- | ---------- | ------------ | -------- | -------- | --------- | --------- | ---------------- | --------------- | ----------- |
| Init - GET Items    | 16,647     | 344          | 614      | 798      | 1,541     | 0.00%     | 74.8             | 388.07          | 16.73       |
| GET /items?cate...  | 11,632     | 344          | 609      | 787      | 1,585     | 0.00%     | 52.3             | 271.29          | 12.46       |
| GET /items/{id} ... | 4,982      | 308          | 433      | 613      | 849       | 0.00%     | 22.4             | 92.57           | 3.50        |
| **TOTAL**           | **33,261** | **338**      | **587**  | **755**  | **1,480** | **0.00%** | **149.5**        | **751.66**      | **32.68**   |

---

## Métriques Grafana

**Période du test** : 00:56:15 → 01:01:00

| Métrique                        | Valeur moyenne   | Valeur pic |
| ------------------------------- | ---------------- | ---------- |
| **CPU Usage - System (%)**      | 100.0%           | 100.0%     |
| **CPU Usage - Process (%)**     | 7.78%            | 7.78%      |
| **Heap Used (%)**               | 0.875%           | -          |
| **Non-Heap Used (%)**           | 91.7%            | -          |
| **GC Pause Time (ms)**          | 0.0100 ms        | 0.0737 ms  |
| **Threads actifs**              | 25-30 (fin: 50+) | 50+        |
| **Requests Per Second (Total)** | 15.6 req/s       | 15.6 req/s |
| **Requests Per Second (GET)**   | 15.6 req/s       | 15.6 req/s |
| **Requests Per Second (POST)**  | 0 req/s          | 0 req/s    |
| **HTTP Error Rate (%)**         | 0%               | -          |
| **Uptime (hours)**              | 0.436 hrs        | -          |

---

## Observations

- **JOIN Query Performance** :

  - GET /items?categoryId (requête avec JOIN) : 346ms moyenne, 344ms médiane, p95 à 787ms
  - GET /items/{id} (requête simple par ID) : 271ms moyenne, 308ms médiane, p95 à 613ms
  - Les requêtes avec filtre categoryId sont ~28% plus lentes que les requêtes par ID direct
  - La latence p99 pour les requêtes JOIN atteint 1,585ms, indiquant des pics de performance sous charge
  - Max response time pour JOIN : 5,801ms (outlier significatif)

- **Filter vs Direct ID Access** :

  - GET par ID direct : Performance meilleure (271ms moyenne vs 346ms)
  - Les requêtes JOIN ont un std. dev. plus élevé (293.82 vs 183.03) - variabilité accrue
  - Throughput similaire proportionnellement au mix (52.7 req/s JOIN vs 22.6 req/s ID)

- **Memory Usage** :

  - Heap très bas (0.875%) - Pas de problème mémoire
  - Non-Heap élevé (91.7%) - À surveiller mais normal pour Spring Boot
  - GC pause time extrêmement faible (0.0100ms moyenne, max 0.0737ms) - Excellent
  - Augmentation des threads vers la fin du test (25-30 → 50+) - Possible saturation

- **Database Query Optimization** :
  - Throughput total élevé : 149.5 req/s (Aggregate Report) / 150.8 req/s (Summary Report)
  - CPU système à 100% - Possible goulot d'étranglement au niveau base de données
  - Toutes les requêtes réussies (0.00% d'erreur) - Système stable
  - **Recommandation** : Optimiser les requêtes JOIN (index sur categoryId) pour réduire la latence p95/p99
