# 📊 Résultats - Scénario HEAVY-body

## Configuration du Test

- **Date** : 2025-11-02
- **Heure début** : ~22:53:00
- **Heure fin** : ~23:01:00
- **Threads** : 30 → 60 (paliers)
- **Ramp-up** : 60s
- **Durée/palier** : 8 min
- **Payload** : 5 KB

## Mix de Requêtes

- 50% : POST /items (5 KB)
- 50% : PUT /items/{id} (5 KB)

---

## Résultats JMeter

### Summary Report

| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| Init - GET Items | 4,428 | 130 | 13 | 1,003 | 41.6 | 0.00% |
| POST /items 5KB (50%) | 2,204 | 174 | 3 | 1,326 | 20.7 | 0.23% |
| PUT /items/{id} 5KB (50%) | 2,189 | 589 | 48 | 3,101 | 20.6 | 0.00% |
| **TOTAL** | **8,821** | **255** | **3** | **3,101** | **82.8** | **0.06%** |

### Aggregate Report

| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 5,337 | 109 | 368 | 477 | 706 | 0.00% |
| POST /items 5KB (50%) | 2,661 | 141 | 585 | 714 | 905 | 0.23% |
| PUT /items/{id} 5KB (50%) | 2,645 | 585 | 1,433 | 1,663 | 2,131 | 0.00% |
| **TOTAL** | **10,643** | **166** | **834** | **1,213** | **1,765** | **0.06%** |

---

## Métriques Grafana

**Période du test** : 22:53:00 → 23:01:00

| Métrique | Valeur moyenne | Valeur pic |
|----------|----------------|------------|
| **CPU Usage - System (%)** | 67.6% | 100% |
| **CPU Usage - Process (%)** | 23.2% | 34.6% |
| **Heap Used (%)** | 1.81% | - |
| **Non-Heap Used (%)** | 86.8% | - |
| **GC Pause Time (ms)** | 1.53 ms | 4.01 ms |
| **Threads actifs** | ~40-50 | 52 |
| **Uptime (hours)** | - | 7.28 hrs |
| **Requests Per Second (Total)** | 90.1 req/s | 245 req/s |
| **Requests Per Second (GET)** | 45.7 req/s | 123 req/s |
| **Requests Per Second (POST)** | 22.2 req/s | 61.3 req/s |
| **HTTP Error Rate (%)** | 48.0% | - |

**Note** : Le taux d'erreur HTTP élevé (48.0%) semble contradictoire avec les résultats JMeter (0.06%). Cela pourrait indiquer un problème de configuration Prometheus/Grafana ou une période différente de test.

---

## Observations

- **Payload Size Impact** : 
  - POST avec 5KB : 174ms moyenne (141ms médiane) - Performance acceptable
  - PUT avec 5KB : 589ms moyenne (585ms médiane) - Significativement plus lent que POST
  - La latence p95 pour PUT est très élevée : 1,663ms, suggérant des pics de performance sous charge

- **Serialization Performance** : 
  - Throughput POST : 20.7 req/s (82.91 KB/s envoyés)
  - Throughput PUT : 20.6 req/s (82.72 KB/s envoyés)
  - Les deux opérations ont un débit similaire en termes de requêtes/seconde

- **Memory Usage** : 
  - Heap très bas (1.81%) - Pas de problème mémoire
  - Non-Heap élevé (86.8%) - Attention à surveiller
  - GC pause time faible (1.53ms moyenne) - Bon signe pour la performance

- **Large Body Handling** : 
  - PUT beaucoup plus lent que POST (589ms vs 174ms)
  - Les requêtes PUT peuvent atteindre 3+ secondes en pic (Max: 3,101ms)
  - Le p99 pour PUT est à 2,131ms, indiquant que 1% des requêtes sont très lentes
  - **Recommandation** : Optimiser les opérations PUT avec payloads lourds (possibles problèmes de lock DB ou de sérialisation) 

