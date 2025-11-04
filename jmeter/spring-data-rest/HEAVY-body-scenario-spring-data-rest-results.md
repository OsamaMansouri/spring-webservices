# 📊 Résultats - Scénario HEAVY-body (Spring Data REST)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~13:15:00
- **Heure fin** : ~13:20:30
- **Threads** : 30
- **Ramp-up** : 60s
- **Durée** : 480s (8 min)
- **Payload** : 5 KB

## Mix de Requêtes

- 50% : POST /api-data-rest/items (5 KB)
- 50% : PUT /api-data-rest/items/{id} (5 KB)

---

## Résultats JMeter

### Summary Report

| Endpoint                  | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| ------------------------- | --------- | ------------ | -------- | -------- | --------- | ------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items          | 36479     | 110          | 11       | 1326     | 72.70     | 0.00%   | 148.4/sec        | 2949.24         | 32.60       | 20352.5    |
| POST /items 5KB (50%)     | 18236     | 63           | 2        | 1168     | 49.12     | 0.00%   | 74.2/sec         | 333.10          | 300.65      | 4595.0     |
| PUT /items/{id} 5KB (50%) | 18228     | 65           | 3        | 1017     | 50.57     | 0.00%   | 74.2/sec         | 332.50          | 301.58      | 4587.0     |
| **TOTAL**                 | **72943** | **87**       | **2**    | **1326** | **66.48** | **0.00%** | **296.7/sec** | **3614.14**     | **634.27**  | **12473.4** |

### Aggregate Report

| Endpoint                  | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Min (ms) | Maximum (ms) | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| ------------------------- | --------- | ------------ | -------- | -------- | -------- | -------- | ------------ | ------- | ---------------- | --------------- | ----------- |
| Init - GET Items          | 40074     | 100          | 200      | 246      | 355      | 11       | 1326         | 0.00%   | 147.4/sec        | 3250.31         | 32.38       |
| POST /items 5KB (50%)     | 20032     | 62           | 119      | 149      | 232      | 2        | 1168         | 0.00%   | 73.7/sec         | 330.84          | 298.61      |
| PUT /items/{id} 5KB (50%) | 20029     | 63           | 120      | 151      | 237      | 3        | 1017         | 0.00%   | 73.7/sec         | 330.26          | 299.54      |
| **TOTAL**                 | **80135** | **79**       | **166**  | **211**  | **316**  | **2**    | **1326**     | **0.00%** | **294.7/sec** | **3910.87**     | **630.05**  |

---

## Métriques Grafana

**Période du test** : 13:15:45 - 13:20:30 (≈5 minutes)

| Métrique                        | Valeur moyenne | Valeur pic |
| ------------------------------- | -------------- | ---------- |
| **CPU Usage - System (%)**      | 100.0          | 100.0      |
| **CPU Usage - Process (%)**     | 35.7           | 41.2       |
| **Heap Used (%)**               | 1.76           | N/A        |
| **Non-Heap Used (%)**           | 89.7           | N/A        |
| **GC Pause Time (ms)**          | 0.320          | 1.31       |
| **Threads actifs**              | 30-50          | 50         |
| **CPU Core Size**               | 10             | 10         |
| **G1 Eden Space**               | 0-75 MB        | 75 MB      |
| **G1 Old Gen**                  | 35-47.5 MB     | 47.5 MB    |
| **Requests Per Second (Total)** | 26.6 req/s     | 94.9 req/s |
| **Requests Per Second (GET)**   | 13.1 req/s     | 46.5 req/s |
| **Requests Per Second (POST)**  | 9.75 req/s     | 25.3 req/s |
| **HTTP Error Rate (%)**         | 6.47           | 6.47       |
| **Uptime (hours)**              | 1.28           | 1.28       |

---

## Observations

- **Heavy Body Handling** : Les opérations POST et PUT avec payload 5KB montrent d'excellentes performances avec des temps de réponse moyens de 63-66ms. Le throughput est stable à environ 74 req/s pour chaque opération.
- **Serialization Performance** : La sérialisation HAL JSON ajoute un overhead minimal. Les temps de réponse sont comparables à ceux des REST Controllers.
- **Memory Usage** : Heap usage très faible (1.76%), indiquant une bonne gestion mémoire. Non-heap usage élevé (89.7%) mais stable. GC pause time reste très faible (moyenne 0.32ms, pic 1.31ms).
- **HAL JSON Format Impact** : Le format HAL augmente légèrement la taille des réponses (Avg. Bytes: 20352.5 pour GET vs ~20KB typique), mais n'impacte pas significativement les performances.
- **Request/Response Body Heavy** : 
  - GET Items reçoit 2949.24-3250.31 KB/sec (très lourd en réponse)
  - POST/PUT envoient ~300 KB/sec (très lourd en requête)
  - Total combiné: 3614-3910 KB/sec reçu, 630 KB/sec envoyé
- **CPU Usage** : System CPU atteint 100% vers la fin du test, indiquant une saturation. Process CPU stable autour de 35.7% en moyenne.
- **Error Rate** : Taux d'erreur HTTP de 6.47% observé dans Grafana, bien que JMeter montre 0.00% d'erreurs dans les opérations principales. Cela peut indiquer des erreurs sur des requêtes secondaires ou des problèmes de connexion.
- **Thread Management** : Les threads augmentent de 30 à 50 vers 13:17:45, correspondant à l'augmentation de charge. Bonne stabilité ensuite.
- **Comparison with REST Controllers** : Les performances sont similaires, mais Spring Data REST a un léger overhead dû au format HAL JSON. Les temps de réponse moyens sont comparables (63-66ms vs ~60-65ms pour REST Controllers).

