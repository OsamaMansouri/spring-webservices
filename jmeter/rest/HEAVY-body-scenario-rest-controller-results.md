# 📊 Résultats - Scénario HEAVY-body (RestController)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~00:36:30
- **Heure fin** : ~00:41:00
- **Threads** : 30
- **Ramp-up** : 60s
- **Durée** : 480s (8 min)
- **Payload** : 5 KB

## Mix de Requêtes

- 50% : POST /api-rest/items (5 KB)
- 50% : PUT /api-rest/items/{id} (5 KB)

---

## Résultats JMeter

### Summary Report

| Endpoint                  | # Samples   | Average (ms) | Min (ms) | Max (ms)  | Std. Dev. | Error %   | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| ------------------------- | ----------- | ------------ | -------- | --------- | --------- | --------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items          | 50,353      | 168          | 18       | 1,585     | 76.86     | 0.00%     | 104.9            | 544.07          | 22.53       | 5312.9     |
| POST /items 5KB (50%)     | 25,168      | 92           | 3        | 1,106     | 61.74     | 5.11%     | 52.4             | 244.97          | 210.17      | 4784.4     |
| PUT /items/{id} 5KB (50%) | 25,162      | 92           | 3        | 1,152     | 56.24     | 0.00%     | 52.4             | 216.37          | 210.89      | 4226.9     |
| **TOTAL**                 | **100,683** | **130**      | **3**    | **1,585** | **78.35** | **1.28%** | **209.7**        | **1005.26**     | **443.46**  | **4909.4** |

### Aggregate Report

| Endpoint                  | # Samples   | Median (p50) | 90th pct | 95th pct | 99th pct | Error %   |
| ------------------------- | ----------- | ------------ | -------- | -------- | -------- | --------- |
| Init - GET Items          | 50,353      | 160          | 256      | 295      | 403      | 0.00%     |
| POST /items 5KB (50%)     | 25,168      | 89           | 153      | 191      | 297      | 5.11%     |
| PUT /items/{id} 5KB (50%) | 25,162      | 90           | 150      | 187      | 277      | 0.00%     |
| **TOTAL**                 | **100,683** | **118**      | **224**  | **264**  | **366**  | **1.28%** |

---

## Métriques Grafana

**Période du test** : 00:36:30 → 00:41:00

| Métrique                        | Valeur moyenne | Valeur pic |
| ------------------------------- | -------------- | ---------- |
| **CPU Usage - System (%)**      | 100%           | 100%       |
| **CPU Usage - Process (%)**     | 11.9%          | 19.1%      |
| **Heap Used (%)**               | 0.634%         | -          |
| **Non-Heap Used (%)**           | 91.0%          | -          |
| **GC Pause Time (ms)**          | 1.05 ms        | 1.77 ms    |
| **Threads actifs**              | ~50            | 50         |
| **Requests Per Second (Total)** | 188 req/s      | 306 req/s  |
| **Requests Per Second (GET)**   | 92.9 req/s     | 153 req/s  |
| **Requests Per Second (POST)**  | 49.5 req/s     | 76.3 req/s |
| **HTTP Error Rate (%)**         | 7.99%          | -          |

---

## Observations

- **Payload Size Impact** :

  - POST avec 5KB : 92ms moyenne (89ms médiane) - Performance excellente
  - PUT avec 5KB : 92ms moyenne (90ms médiane) - Performance similaire à POST
  - Les opérations POST/PUT sont plus rapides que GET (168ms moyenne) malgré le payload lourd
  - La latence p95 pour POST est à 191ms, et pour PUT à 187ms - performances stables

- **Serialization Performance** :

  - Throughput POST : 52.4 req/s (210.17 KB/s envoyés)
  - Throughput PUT : 52.4 req/s (210.89 KB/s envoyés)
  - Les deux opérations ont un débit identique en termes de requêtes/seconde
  - Total throughput : 209.7 req/s, avec GET dominant (104.9 req/s)

- **Memory Usage** :

  - Heap très bas (0.634%) - Pas de problème mémoire
  - Non-Heap élevé (91.0%) - À surveiller mais normal pour Spring Boot
  - GC pause time faible (1.05ms moyenne, max 1.77ms) - Excellent signe pour la performance

- **Large Body Handling** :
  - POST et PUT ont des performances similaires (92ms moyenne) malgré les payloads de 5KB
  - Taux d'erreur POST : 5.11% (contributeur principal au 1.28% total)
  - PUT sans erreurs (0.00%) - Performance très stable
  - **Recommandation** : Investiguer les erreurs POST (5.11%) - possible problème de validation ou de contraintes DB
