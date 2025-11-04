# 📊 Résultats - Scénario JOIN-filter

## Configuration du Test

- **Date** : 2025-11-02
- **Heure début** : ~23:15:00
- **Heure fin** : ~23:25:00
- **Threads** : 60 → 120 (paliers)
- **Ramp-up** : 60s
- **Durée/palier** : 8 min
- **Payload** : Néant (lectures uniquement)

## Mix de Requêtes

- 70% : GET /items?categoryId=...&page=&size=
- 30% : GET /items/{id}

---

## Résultats JMeter

### Summary Report

| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| GET /items?categoryId (70%) | 4,873 | 413 | 14 | 3,068 | 39.9 | 0.00% |
| GET /items/{id} (30%) | 2,086 | 317 | 1 | 2,485 | 17.1 | 0.00% |
| **TOTAL** | **13,960** | **399** | **1** | **3,071** | **113.7** | **0.00%** |

### Aggregate Report

| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| GET /items?categoryId (70%) | 5,924 | 426 | 805 | 1,131 | 1,564 | 0.00% |
| GET /items/{id} (30%) | 2,537 | 389 | 479 | 520 | 907 | 0.00% |
| **TOTAL** | **16,954** | **419** | **638** | **1,053** | **1,497** | **0.00%** |

---

## Métriques Grafana

**Période du test** : ~23:15:00 → ~23:25:00

| Métrique | Valeur moyenne | Valeur pic |
|----------|----------------|------------|
| **CPU Usage - System (%)** | ~75-100% | 100% |
| **CPU Usage - Process (%)** | ~25-50% | - |
| **Heap Used (%)** | 0.782% | - |
| **Non-Heap Used (%)** | 95.2% | - |
| **GC Pause Time (ms)** | ~5-28 ms | ~28 ms |
| **Threads actifs** | ~50-53 | 53 |
| **Uptime (hours)** | - | 7.37 hrs |
| **Requests Per Second (Total)** | ~42.8 req/s | - |
| **Requests Per Second (GET)** | ~21.6 req/s | - |
| **Requests Per Second (POST)** | ~10.6 req/s | - |
| **HTTP Error Rate (%)** | 0.0738% | - |

**Note** : La présence de requêtes POST dans Grafana est inattendue pour un scénario JOIN-filter (lectures uniquement). Cela pourrait indiquer une période de test différente ou des métriques agrégées.

---

## Observations

- **JOIN Performance** : 
  - GET /items?categoryId (requête avec JOIN) : 413ms moyenne, p95 à 1,131ms
  - GET /items/{id} (requête simple par ID) : 317ms moyenne, p95 à 520ms
  - Les requêtes avec filtre categoryId sont ~30% plus lentes que les requêtes par ID direct
  - La latence p99 pour les requêtes JOIN atteint 1,564ms, indiquant des pics de performance sous charge

- **N+1 Query Issues** : 
  - Les temps de réponse sont acceptables pour des requêtes avec JOIN
  - Pas de signes évidents de problèmes N+1 (les requêtes seraient beaucoup plus lentes si c'était le cas)

- **Database Load** : 
  - Throughput total élevé : 113.7 req/s (Summary Report)
  - CPU système très élevé (75-100%) - possible goulot d'étranglement au niveau base de données
  - HikariCP connections : ~10-15 actives / max ~10 - possible saturation du pool de connexions
  - **Recommandation** : Augmenter le pool de connexions HikariCP pour ce type de scénario JOIN-intensive 

