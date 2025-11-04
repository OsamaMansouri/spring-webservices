# 📊 Résultats - Scénario MIXED

## Configuration du Test

- **Date** : 2025-11-02
- **Heure début** : ~23:30:00
- **Heure fin** : ~23:40:00
- **Threads** : 50 → 100 (paliers)
- **Ramp-up** : 60s
- **Durée/palier** : 10 min
- **Payload** : 1 KB

## Mix de Requêtes

- 40% : GET /items?page=...
- 20% : POST /items (1 KB)
- 10% : PUT /items/{id} (1 KB)
- 10% : DELETE /items/{id}
- 10% : POST /categories (0.5-1 KB)
- 10% : PUT /categories/{id}

---

## Résultats JMeter

### Summary Report

| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| GET /items (40%) | 24,292 | 158 | 15 | 1,201 | 42.4 | 0.00% |
| POST /items (20%) | 12,146 | 163 | 2 | 1,432 | 21.2 | 1.16% |
| PUT /items/{id} (10%) | 6,074 | 114 | 1 | 785 | 10.6 | 0.00% |
| DELETE /items/{id} (10%) | 6,072 | 113 | 1 | 1,055 | 10.6 | 0.00% |
| POST /categories (10%) | 6,070 | 118 | 2 | 903 | 10.6 | 0.44% |
| PUT /categories/{id} (10%) | 6,067 | 173 | 4 | 1,314 | 10.6 | 0.05% |
| **TOTAL** | **182,228** | **140** | **1** | **1,432** | **317.8** | **0.09%** |

### Aggregate Report

| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| GET /items (40%) | 25,185 | 147 | 277 | 345 | 519 | 0.00% |
| POST /items (20%) | 12,592 | 133 | 326 | 403 | 594 | 1.17% |
| PUT /items/{id} (10%) | 6,296 | 107 | 232 | 300 | 458 | 0.00% |
| DELETE /items/{id} (10%) | 6,295 | 106 | 228 | 295 | 459 | 0.00% |
| POST /categories (10%) | 6,292 | 109 | 233 | 299 | 464 | 0.45% |
| PUT /categories/{id} (10%) | 6,291 | 145 | 340 | 424 | 629 | 0.05% |
| **TOTAL** | **188,925** | **128** | **267** | **336** | **507** | **0.09%** |

---

## Métriques Grafana

**Période du test** : ~23:30:00 → ~23:40:00

| Métrique | Valeur moyenne | Valeur pic |
|----------|----------------|------------|
| **CPU Usage - System (%)** | 100% | 100% |
| **CPU Usage - Process (%)** | 20.4% | 21.3% |
| **Heap Used (%)** | 8.06% | - |
| **Non-Heap Used (%)** | 86.6% | - |
| **GC Pause Time (ms)** | 12.6 ms | 26.7 ms |
| **Threads actifs** | ~80 | 80 |
| **Uptime (hours)** | - | 7.46 hrs |
| **Requests Per Second (Total)** | 77.5 req/s | 115 req/s |
| **Requests Per Second (GET)** | 69.8 req/s | 107 req/s |
| **Requests Per Second (POST)** | 3.87 req/s | 9.16 req/s |
| **HTTP Error Rate (%)** | 0.00353% | - |

---

## Observations

- **Écritures vs Lectures** : 
  - **Lectures (GET)** : 158ms moyenne, 0% erreur - Performance excellente et stable
  - **Écritures (POST/PUT)** : 
    - POST /items : 163ms moyenne mais 1.17% erreur (le plus élevé)
    - POST /categories : 118ms moyenne, 0.45% erreur
    - PUT /categories : 173ms moyenne (le plus lent des écritures), 0.05% erreur
  - **Suppressions (DELETE)** : 113ms moyenne, 0% erreur - Très performant

- **Transaction Performance** : 
  - Throughput total élevé : 317.8 req/s (JMeter) vs 77.5 req/s (Grafana) - Possible différence de période de mesure
  - Latence p95 globale : 336ms - Acceptable pour un scénario mixte
  - Latence p99 globale : 507ms - Quelques pics mais tolérable
  - PUT /categories a la latence p99 la plus élevée : 629ms

- **Database Contention** : 
  - CPU système à 100% - Goulot d'étranglement CPU probable
  - Taux d'erreur très bas (0.09% global) malgré la charge mixte
  - POST /items a le taux d'erreur le plus élevé (1.17%) - Possible contention lors de la création d'items
  - Les opérations DELETE et PUT sont très rapides (113-114ms) - Pas de contention visible sur les mises à jour/suppressions

- **Memory & GC** :
  - Heap utilisé faible (8.06%) - Pas de problème mémoire
  - Non-Heap élevé (86.6%) - À surveiller
  - GC pause time acceptable (12.6ms moyenne) - Bonne gestion mémoire
  - Threads stables à 80 - Cohérent avec la charge

- **Recommandations** :
  - Investiguer les erreurs POST /items (1.17%) - Possible problème de contrainte unique (SKU) ou de validation
  - Optimiser PUT /categories (latence p99: 629ms)
  - Augmenter le pool de connexions HikariCP si le CPU système est le goulot 

