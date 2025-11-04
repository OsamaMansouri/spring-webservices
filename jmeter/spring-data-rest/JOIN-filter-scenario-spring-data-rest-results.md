# 📊 Résultats - Scénario JOIN-filter (Spring Data REST)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~13:23:30
- **Heure fin** : ~13:28:30
- **Threads** : 60
- **Ramp-up** : 60s
- **Durée** : 480s (8 min)

## Mix de Requêtes

- 50% : GET /api-data-rest/items/search/byCategory?categoryId={id}
- 30% : GET /api-data-rest/items/{id}
- 20% : GET /api-data-rest/items

---

## Résultats JMeter

### Summary Report

| Endpoint                      | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| ----------------------------- | --------- | ------------ | -------- | -------- | --------- | ------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items              | 34735     | 394          | 16       | 2258     | 196.61    | 0.00%   | 72.3/sec         | 3966.50         | 16.52       | 56168.5    |
| GET /items/search/byCategory (50%)| 24298  | 404          | 19       | 2670     | 185.11    | 0.00%   | 50.6/sec         | 27.30           | 13.10       | 552.7      |
| GET /items/{id} (30%)         | 10411     | 317          | 2        | 1594     | 180.42    | 0.00%   | 21.7/sec         | 96.02           | 3.45        | 4533.7     |
| **TOTAL**                     | **69444** | **386**      | **2**    | **2670** | **192.52** | **0.00%** | **144.6/sec** | **4089.63**     | **33.07**   | **28967.8** |

### Aggregate Report

| Endpoint                      | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Min (ms) | Maximum (ms) | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| ----------------------------- | --------- | ------------ | -------- | -------- | -------- | -------- | ------------ | ------- | ---------------- | --------------- | ----------- |
| Init - GET Items              | 34735     | 391          | 599      | 737      | 1029     | 16       | 2258         | 0.00%   | 72.3/sec         | 3966.50         | 16.52       |
| GET /items/search/byCategory (50%)| 24298  | 405          | 588      | 735      | 985      | 19       | 2670         | 0.00%   | 50.6/sec         | 27.30           | 13.10       |
| GET /items/{id} (30%)         | 10411     | 328          | 469      | 634      | 914      | 2        | 1594         | 0.00%   | 21.7/sec         | 96.02           | 3.45        |
| **TOTAL**                     | **69444** | **386**      | **583**  | **724**  | **1000** | **2**    | **2670**     | **0.00%** | **144.6/sec** | **4089.63**     | **33.07**   |

---

## Métriques Grafana

**Période du test** : 13:23:30 - 13:28:30 (≈5 minutes)

| Métrique                        | Valeur moyenne | Valeur pic |
| ------------------------------- | -------------- | ---------- |
| **CPU Usage - System (%)**      | 100.0          | 100.0      |
| **CPU Usage - Process (%)**     | 16.3           | 20.2       |
| **Heap Used (%)**               | N/A            | N/A        |
| **Non-Heap Used (%)**           | N/A            | N/A        |
| **GC Pause Time (ms)**          | 2.92           | 3.68       |
| **Threads actifs**              | 50-80          | 80         |
| **CPU Core Size**               | 10             | 10         |
| **Threads vs CPU Cores**        | 92.1%          | 92.1%      |
| **G1 Eden Space**               | 0-90 MB        | 90 MB      |
| **G1 Old Gen**                  | 46-52 MB       | 52 MB      |
| **Requests Per Second (Total)** | 191 req/s      | 269 req/s  |
| **Requests Per Second (GET)**   | 133 req/s      | 141 req/s  |
| **Requests Per Second (POST)**  | 29.1 req/s     | 67.3 req/s |
| **HTTP Error Rate (%)**         | 0.00           | 0.00       |
| **Uptime (hours)**              | 1.42           | 1.42       |

---

## Observations

- **JOIN Query Performance** : Les requêtes avec filtre par catégorie (`/items/search/byCategory`) montrent des performances légèrement inférieures (404ms moyenne) comparées aux requêtes directes par ID (317ms moyenne). Cela indique un overhead des requêtes JOIN en base de données.
- **Filter vs Direct ID Access** : 
  - GET par ID (317ms) est plus rapide que GET par categoryId (404ms)
  - Les deux types de requêtes montrent des temps de réponse acceptables avec 0% d'erreurs
  - Le throughput pour les filtres par catégorie est plus élevé (50.6 req/s) que pour les GET par ID (21.7 req/s), reflétant la répartition 50%/30% du mix
- **HAL JSON Format Impact** : Le format HAL JSON est visible dans les réponses, mais n'impacte pas significativement les performances. La taille moyenne des réponses varie selon l'endpoint (552.7 bytes pour les filtres, 4533.7 bytes pour GET par ID, 56168.5 bytes pour GET Items complet).
- **Memory Usage** : G1 Eden Space montre un pattern de GC fréquent (0-90 MB), typique d'une application Java sous charge. G1 Old Gen augmente progressivement de 46 MB à 52 MB, indiquant une accumulation mémoire normale.
- **Database Query Optimization** : Les temps de réponse moyens (317-404ms) sont cohérents avec des requêtes JOIN sur des grandes tables. Spring Data REST utilise efficacement les index pour optimiser les requêtes filtrées.
- **CPU Usage** : System CPU à 100%, indiquant une saturation complète du système. Process CPU reste modéré à 16.3% en moyenne, suggérant que la charge est répartie entre plusieurs processus.
- **Thread Management** : Les threads augmentent de 50 à 80 pendant le test, atteignant un ratio de 92.1% vs CPU cores (10 cores). Ce ratio élevé peut expliquer la saturation CPU.
- **Throughput** : Excellent throughput total de 144.6 req/s avec une répartition cohérente : 50.6 req/s pour les filtres, 21.7 req/s pour GET par ID, et 72.3 req/s pour l'initialisation.
- **Response Size** : Init GET Items reçoit énormément de données (3966.50 KB/sec), reflétant la récupération de grandes collections. Les requêtes filtrées sont beaucoup plus légères (27.30 KB/sec).
- **GC Performance** : GC pause time moyen de 2.92ms (pic 3.68ms) est acceptable mais plus élevé que dans le scénario HEAVY-body, probablement dû à la charge continue de requêtes READ.
- **Comparison with REST Controllers** : Les performances sont comparables, avec un léger overhead dû au format HAL JSON. Les temps de réponse sont similaires, confirmant que Spring Data REST gère efficacement les requêtes JOIN complexes.

