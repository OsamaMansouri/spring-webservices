# 📊 Résultats - Scénario MIXED (RestController)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~10:20:00
- **Heure fin** : ~10:24:45
- **Threads** : 50
- **Ramp-up** : 60s
- **Durée** : 600s (10 min)

## Mix de Requêtes

- 40% : GET /api-rest/items
- 20% : POST /api-rest/items
- 10% : PUT /api-rest/items/{id}
- 10% : DELETE /api-rest/items/{id}
- 10% : POST /api-rest/categories
- 10% : PUT /api-rest/categories/{id}

---

## Résultats JMeter

### Summary Report

| Endpoint                   | # Samples  | Average (ms) | Min (ms) | Max (ms)   | Std. Dev.  | Error %     | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| -------------------------- | ---------- | ------------ | -------- | ---------- | ---------- | ----------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items           | 17,806     | 436          | 20       | 22,846     | 605.85     | 0.00%       | 43.4             | 224.92          | 9.36        | 5312.8     |
| Init - GET Categories      | 17,793     | 287          | 3        | 1,285      | 125.97     | 0.00%       | 43.4             | 106.69          | 9.53        | 2519.9     |
| GET /items (40%)           | 7,110      | 426          | 31       | 24,013     | 532.81     | 0.00%       | 17.3             | 89.95           | 3.78        | 5312.8     |
| POST /items (20%)          | 3,555      | 292          | 3        | 1,080      | 123.10     | 6.55%       | 8.7              | 11.74           | 2.50        | 1384.7     |
| PUT /items/{id} (10%)      | 1,778      | 288          | 2        | 1,212      | 126.30     | **100.00%** | 4.3              | 0.51            | 1.32        | 120.8      |
| DELETE /items/{id} (10%)   | 1,777      | 291          | 1        | 1,024      | 122.86     | **100.00%** | 4.3              | 0.51            | 0.91        | 120.9      |
| POST /categories (10%)     | 1,776      | 291          | 3        | 1,207      | 124.07     | 0.34%       | 4.3              | 1.36            | 1.09        | 319.6      |
| PUT /categories/{id} (10%) | 1,775      | 291          | 3        | 1,306      | 129.87     | 0.06%       | 4.3              | 1.22            | 1.16        | 287.8      |
| **TOTAL**                  | **53,370** | **356**      | **1**    | **24,013** | **417.03** | **7.11%**   | **129.9**        | **436.63**      | **29.60**   | **3440.9** |

**Note Summary Report** : Les valeurs # Samples peuvent différer légèrement entre Summary Report et Aggregate Report car ils sont calculés à des moments différents du test.

### Aggregate Report

| Endpoint                   | # Samples  | Median (p50) | 90th pct | 95th pct | 99th pct  | Min (ms) | Maximum (ms) | Error %     | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| -------------------------- | ---------- | ------------ | -------- | -------- | --------- | -------- | ------------ | ----------- | ---------------- | --------------- | ----------- |
| Init - GET Items           | 18,999     | 369          | 571      | 729      | 2,326     | 20       | 22,846       | 0.00%       | 43.4             | 225.01          | 9.36        |
| Init - GET Categories      | 18,992     | 296          | 402      | 482      | 656       | 3        | 1,285        | 0.00%       | 43.4             | 106.88          | 9.53        |
| GET /items (40%)           | 7,587      | 369          | 575      | 717      | 2,138     | 31       | 24,013       | 0.00%       | 17.3             | 89.97           | 3.78        |
| POST /items (20%)          | 3,797      | 300          | 404      | 485      | 664       | 3        | 1,080        | 6.45%       | 8.7              | 11.62           | 2.50        |
| PUT /items/{id} (10%)      | 1,897      | 295          | 400      | 481      | 674       | 2        | 1,212        | **100.00%** | 4.3              | 0.51            | 1.32        |
| DELETE /items/{id} (10%)   | 1,895      | 295          | 400      | 487      | 664       | 1        | 1,024        | **100.00%** | 4.3              | 0.51            | 0.91        |
| POST /categories (10%)     | 1,894      | 300          | 403      | 475      | 621       | 3        | 1,207        | 0.42%       | 4.3              | 1.41            | 1.09        |
| PUT /categories/{id} (10%) | 1,893      | 296          | 407      | 498      | 662       | 3        | 1,306        | 0.05%       | 4.3              | 1.22            | 1.16        |
| **TOTAL**                  | **56,954** | **327**      | **483**  | **602**  | **1,535** | **1**    | **24,013**   | **7.10%**   | **130.0**        | **436.90**      | **29.62**   |

---

## Métriques Grafana

**Période du test** : 10:20:00 → 10:24:45

| Métrique                        | Valeur moyenne | Valeur pic |
| ------------------------------- | -------------- | ---------- |
| **CPU Usage - System (%)**      | 100%           | 100%       |
| **CPU Usage - System (Min)**    | 30.5%          | -          |
| **CPU Usage - Process (%)**     | 9.33%          | 11.2%      |
| **CPU Usage - Process (Min)**   | 0.338%         | -          |
| **Heap Used (%)**               | 0.811%         | -          |
| **Non-Heap Used (%)**           | 92.7%          | -          |
| **G1 Eden Space (heap)**        | ~20-25 MB      | ~45 MB     |
| **G1 Old Gen (heap)**           | ~44-46 MB      | ~46 MB     |
| **GC Pause Time (ms)**          | 0.661 ms       | 1.31 ms    |
| **Threads actifs**              | ~50-70         | 70         |
| **CPU Core Size**               | ~20 cores      | -          |
| **Requests Per Second (Total)** | 63.6 req/s     | 132 req/s  |
| **Requests Per Second (GET)**   | 50.9 req/s     | 106 req/s  |
| **Requests Per Second (POST)**  | 6.34 req/s     | 13.2 req/s |
| **HTTP Error Rate (%)**         | 7.08%          | -          |
| **Uptime (hours)**              | 9.83 hrs       | -          |

---

## Observations

### ⚠️ Problèmes Critiques

- **PUT /items/{id}** : **100% d'erreurs** - Toutes les requêtes PUT échouent

  - 1,778 échantillons, tous en erreur
  - Temps de réponse moyen : 288ms (mais toutes les requêtes retournent une erreur)
  - Throughput : 4.3 req/s
  - **Cause probable** : L'extraction de `itemId` depuis le GET initial ne fonctionne pas correctement, ou les items utilisés n'existent pas/ont été supprimés

- **DELETE /items/{id}** : **100% d'erreurs** - Toutes les requêtes DELETE échouent

  - 1,777 échantillons, tous en erreur
  - Temps de réponse moyen : 291ms (mais toutes les requêtes retournent une erreur)
  - Throughput : 4.3 req/s
  - **Cause probable** : Même problème que PUT - extraction d'ID ou items inexistants

- **POST /items** : **6.45% d'erreurs** - Taux d'erreur significatif
  - 3,797 échantillons, 245 erreurs
  - Possible problème de validation ou contraintes base de données

### ✅ Opérations Réussies

- **GET /items (40%)** : **0.00% d'erreurs** - Performance excellente

  - 7,587 échantillons, tous réussis
  - Temps de réponse moyen : 428ms, médiane : 369ms
  - P95 : 717ms, P99 : 2,138ms
  - Throughput : 17.3 req/s

- **Init - GET Items** : **0.00% d'erreurs** - Stable

  - 18,999 échantillons, tous réussis
  - Temps de réponse moyen : 437ms, médiane : 369ms
  - Throughput : 43.4 req/s

- **Init - GET Categories** : **0.00% d'erreurs** - Performance meilleure que Items

  - 18,992 échantillons, tous réussis
  - Temps de réponse moyen : 288ms, médiane : 296ms
  - P95 : 482ms, P99 : 656ms
  - Throughput : 43.4 req/s

- **POST /categories** : **0.42% d'erreurs** - Très faible taux

  - 1,894 échantillons, ~8 erreurs
  - Temps de réponse moyen : 293ms, médiane : 300ms

- **PUT /categories/{id}** : **0.05% d'erreurs** - Excellent
  - 1,893 échantillons, ~1 erreur
  - Temps de réponse moyen : 292ms, médiane : 296ms

### Performance Générale

- **Throughput Total** : 130.0 req/s (129.9 req/s Summary Report)

  - GET dominant : ~60.7 req/s (Init GET + GET items)
  - POST : ~13.0 req/s
  - PUT/DELETE : 8.6 req/s (mais 100% d'erreurs)

- **Latence** :

  - Médiane globale : 327ms
  - P95 : 602ms
  - P99 : 1,535ms
  - Max : 24,013ms (outlier significatif sur GET)

- **Memory Usage** :

  - Heap très bas (0.811%) - Pas de problème mémoire
  - Non-Heap élevé (92.7%) - À surveiller mais normal pour Spring Boot
  - GC pause time faible (0.661ms moyenne, max 1.31ms) - Excellent

- **CPU Usage** :

  - CPU système à 100% - Goulot d'étranglement possible au niveau système/base de données
  - CPU processus : 9.33% moyenne - Application stable

- **Threads** :
  - ~50-70 threads actifs pendant le test
  - Stable après montée en charge initiale

### Recommandations

1. **URGENT - Corriger PUT/DELETE Items** :

   - Vérifier l'extraction de `itemId` depuis la réponse GET initial
   - S'assurer que les items existent dans la base avant les opérations PUT/DELETE
   - Ajouter une validation préalable que l'item existe avant PUT/DELETE
   - Possible solution : Utiliser un ID statique garanti d'exister, ou créer un item au début de chaque thread

2. **Investiguer POST /items (6.45% d'erreurs)** :

   - Analyser les logs pour comprendre les causes d'erreur
   - Possible problème de validation, contraintes DB, ou conflits de données

3. **Optimiser les requêtes GET** :

   - Max response time très élevé (24,013ms) - possible problème de base de données ou réseau
   - P99 à 2,326ms pour Init GET - considérer l'optimisation des requêtes

4. **Monitoring** :
   - CPU système à 100% - vérifier la charge système et base de données
   - Non-Heap à 92.7% - surveiller la mémoire métaspace/permgen

---

## Conclusion

Le test MIXED révèle un **problème critique avec PUT/DELETE sur les items (100% d'erreurs)**, ce qui indique un problème dans la logique d'extraction d'ID ou la disponibilité des items. Les opérations GET et les opérations sur les categories fonctionnent bien. Le taux d'erreur global de 7.10% est principalement dû aux erreurs PUT/DELETE. Une correction urgente est nécessaire pour ces opérations avant de considérer ce scénario comme validé.
