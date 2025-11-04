# 📊 Résultats - Scénario READ-heavy

## Configuration du Test

- **Date** : 2025-11-02
- **Heure début** : ~22:29:00
- **Heure fin** : ~22:34:00
- **Threads** : 50 → 100 → 200 (paliers)
- **Ramp-up** : 60s
- **Durée/palier** : 10 min
- **Payload** : Néant (lectures uniquement)

## Mix de Requêtes

- 50% : GET /items?page=&size=50
- 20% : GET /items?categoryId=...&page=&size=
- 20% : GET /categories/{id}/items?page=&size=
- 10% : GET /categories?page=&size=

---

## Résultats JMeter

### Summary Report

| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| GET /items (50%) | 62,220 | 487 | 0 | 2,476,294 | 17.0 | 6.80% |
| GET /items?categoryId... | 24,884 | 568 | 0 | 2,472,690 | 6.8 | 6.81% |
| GET /categories/{id}/items | 24,872 | 1,043 | 0 | 2,476,775 | 6.8 | 6.80% |
| GET /categories | 12,440 | 935 | 0 | 2,471,945 | 3.4 | 6.81% |
| **TOTAL** | **248,903** | **673** | **0** | **2,476,775** | **68.0** | **6.80%** |

### Aggregate Report

| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| GET /items (50%) | 62,220 | 165 | 332 | 427 | 683 | 6.80% |
| GET /items?categoryId... | 24,884 | 165 | 333 | 424 | 675 | 6.81% |
| GET /categories/{id}/items | 24,872 | 213 | 501 | 633 | 964 | 6.80% |
| GET /categories | 12,440 | 137 | 303 | 392 | 652 | 6.81% |
| **TOTAL** | **248,903** | **165** | **350** | **451** | **730** | **6.80%** |

---

## Métriques Grafana

**Période du test** : ~22:29:00 → ~22:34:00

| Métrique | Valeur moyenne | Valeur pic |
|----------|----------------|------------|
| **CPU Usage - System (%)** | ~70-75% | ~75% |
| **CPU Usage - Process (%)** | ~20-25% | ~25% |
| **Heap Used (%)** | 1.34% | - |
| **Non-Heap Used (%)** | 85.1% | - |
| **GC Pause Time (ms)** | 0.573 ms | 0.823 ms |
| **Threads actifs** | ~30-70 | 70 |
| **Uptime (hours)** | - | 6.82 hrs |
| **Requests Per Second (Total)** | 21.9 req/s | 92.2 req/s |
| **Requests Per Second (GET)** | 21.9 req/s | 92.2 req/s |
| **HTTP Error Rate (%)** | 0% (No data) | - |

**Note** : L'absence d'erreurs dans Grafana (0%) contraste avec les 6.80% d'erreurs dans JMeter. Cela pourrait indiquer que les erreurs sont des timeouts plutôt que des erreurs HTTP 4xx/5xx, ou que la période mesurée dans Grafana est différente.

---

## Observations

- **Taux d'erreur élevé et constant (6.80-6.81%)** : 
  - Un taux d'erreur de ~6.80% pour un scénario de lecture uniquement est préoccupant
  - Tous les endpoints ont le même taux d'erreur, suggérant un problème systémique (timeouts DB, pool de connexions saturé, ou problème réseau)
  - Les erreurs ne sont pas visibles dans Grafana HTTP Error Rate, ce qui indique qu'il s'agit probablement de timeouts plutôt que d'erreurs HTTP 4xx/5xx

- **Temps de réponse maximum extrêmes** : 
  - Max response times : ~2.4 millions de ms (≈41 minutes) - Très anormal
  - Ces valeurs extrêmes suggèrent des requêtes qui ont timeout ou ont été abandonnées après un très long délai
  - La médiane (p50) est beaucoup plus raisonnable (137-213ms), indiquant que la majorité des requêtes réussissent

- **Performance par endpoint** : 
  - **GET /categories** : 137ms médiane (la plus rapide), 935ms moyenne - Bonne performance quand réussi
  - **GET /items (50%)** : 165ms médiane, 487ms moyenne - Performance acceptable
  - **GET /items?categoryId** : 165ms médiane, 568ms moyenne - Performance similaire à GET /items
  - **GET /categories/{id}/items** : 213ms médiane, 1,043ms moyenne (la plus lente) - Plus complexe avec JOIN/relation inverse
  - La latence p99 pour GET /categories/{id}/items atteint 964ms, suggérant des pics de performance

- **Throughput** : 
  - Total : 68.0 req/s (JMeter) vs 21.9-92.2 req/s (Grafana)
  - GET /items (50%) : 17.0 req/s - Contribue le plus au débit
  - Distribution conforme aux pourcentages attendus (50/20/20/10)

- **Ressources système** : 
  - CPU système : 70-75% - Élevé mais pas saturé comme dans MIXED
  - CPU process : 20-25% - Utilisation raisonnable
  - Heap : 1.34% - Très faible, pas de problème mémoire
  - Non-Heap : 85.1% - Élevé, à surveiller
  - GC pause time : 0.573ms moyenne - Excellent, pas de problème GC
  - Threads : Augmente de 30 à 70 pendant le test - Normal sous charge

- **Recommandations** : 
  - **Investigation urgente** : Analyser les logs pour identifier la cause des 6.80% d'erreurs (probablement timeouts DB)
  - Vérifier le pool de connexions HikariCP - Possible saturation
  - Optimiser GET /categories/{id}/items (le plus lent) - Possible problème N+1 ou JOIN inefficace
  - Les temps de réponse maximum extrêmes suggèrent un problème de configuration de timeout ou de pool de connexions
  - Comparer avec les autres scénarios : Le taux d'erreur est beaucoup plus élevé que MIXED (0.09%) et JOIN-filter (0.00%) 

