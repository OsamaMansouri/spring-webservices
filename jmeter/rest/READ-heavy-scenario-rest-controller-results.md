# 📊 Résultats - Scénario READ-heavy (RestController)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~10:49:00
- **Heure fin** : ~10:53:45
- **Threads** : [À déterminer]
- **Ramp-up** : [À déterminer]
- **Durée** : [À déterminer]

## Mix de Requêtes

- 50% : GET /api-rest/items
- 20% : GET /api-rest/items?categoryId={id}
- 20% : GET /api-rest/categories/{id}
- 10% : GET /api-rest/categories

---

## Résultats JMeter

### Summary Report

| Endpoint                      | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| ----------------------------- | --------- | ------------ | -------- | -------- | --------- | ------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items              | 867       | 597          | 74       | 5,769    | 714.85    | 0.00%   | 18.0             | 93.48           | 4.03        | 5313.0     |
| GET /items (50%)              | 422       | 591          | 91       | 6,283    | 676.08    | 0.00%   | 8.9              | 46.41           | 2.01        | 5313.0     |
| GET /items?categoryId... (20%)| 164       | 595          | 85       | 4,930    | 700.58    | 0.00%   | 3.5              | 18.41           | 0.85        | 5313.0     |
| GET /categories/{id} (20%)    | 161       | 430          | 87       | 1,087    | 230.40    | 0.00%   | 3.5              | 17.22           | 0.85        | 4989.0     |
| GET /categories (10%)         | 82        | 337          | 13       | 1,546    | 268.85    | 0.00%   | 1.8              | 4.63            | 0.43        | 2567.0     |
| **TOTAL**                     | **1,696** | **567**      | **13**   | **6,283**| **660.29**| **0.00%**| **35.2**         | **177.07**      | **8.00**    | **5149.5** |

**Note Summary Report** : Les valeurs # Samples peuvent différer légèrement entre Summary Report et Aggregate Report car ils sont calculés à des moments différents du test.

### Aggregate Report

| Endpoint                      | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Min (ms) | Maximum (ms) | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| ----------------------------- | --------- | ------------ | -------- | -------- | -------- | -------- | ------------ | ------- | ---------------- | --------------- | ----------- |
| Init - GET Items              | 1,620     | 839          | 1,143    | 1,307    | 4,904    | 74       | 9,652        | 0.00%   | 19.5             | 101.22          | 4.36        |
| GET /items (50%)              | 795       | 828          | 1,136    | 1,302    | 4,616    | 91       | 6,283        | 0.00%   | 9.7              | 50.34           | 2.18        |
| GET /items?categoryId... (20%)| 315       | 832          | 1,149    | 1,483    | 4,953    | 85       | 5,722        | 0.00%   | 3.9              | 20.13           | 0.92        |
| GET /categories/{id} (20%)    | 310       | 774          | 1,054    | 1,099    | 1,272    | 75       | 2,021        | 0.00%   | 3.8              | 18.70           | 0.92        |
| GET /categories (10%)         | 158       | 709          | 981      | 1,046    | 1,146    | 13       | 1,546        | 0.00%   | 2.0              | 4.98            | 0.46        |
| **TOTAL**                     | **3,199** | **826**      | **1,126**| **1,260**| **4,780**| **13**   | **9,652**    | **0.00%**| **38.5**         | **193.54**      | **8.75**    |

---

## Métriques Grafana

**Période du test** : 10:49:00 → 10:53:45

| Métrique                        | Valeur moyenne | Valeur pic |
| ------------------------------- | -------------- | ---------- |
| **CPU Usage - System (%)**      | 100%           | 100%       |
| **CPU Usage - System (Min)**    | 100%           | -          |
| **CPU Usage - Process (%)**     | 5.26%          | 7.33%      |
| **CPU Usage - Process (Min)**   | 5.26%          | -          |
| **Heap Used (%)**               | 1.10%          | -          |
| **Non-Heap Used (%)**           | 91.3%          | -          |
| **G1 Eden Space (heap)**        | ~0-40 MB       | ~40 MB     |
| **G1 Old Gen (heap)**           | ~44-45.5 MB    | ~45.5 MB   |
| **GC Pause Time (ms)**          | 0.537 ms       | 0.769 ms   |
| **Threads actifs**              | ~72-73         | 73         |
| **CPU Core Size**               | ~60 cores      | -          |
| **Requests Per Second (Total)** | 44.8 req/s     | 44.8 req/s |
| **Requests Per Second (GET)**   | 44.8 req/s     | 44.8 req/s |
| **Requests Per Second (POST)**  | 0 req/s        | 0 req/s    |
| **HTTP Error Rate (%)**         | 0%             | -          |
| **Uptime (hours)**              | 10.3 hrs       | -          |

---

## Observations

### ✅ Performance Excellente - 0% d'Erreurs

- **Toutes les requêtes réussies** : **0.00% d'erreurs** sur tous les endpoints
  - Scénario READ-heavy parfaitement stable
  - Aucune requête en échec sur 3,199 échantillons (Aggregate Report)
  - Aucune requête en échec sur 1,696 échantillons (Summary Report)

### Performance des Endpoints

- **Init - GET Items** : **Performance correcte mais latence élevée**
  - 1,620 échantillons (Aggregate) / 867 (Summary)
  - Temps de réponse moyen : 597ms (Summary) / 846ms (Aggregate)
  - Médiane : 839ms, P95 : 1,307ms, P99 : 4,904ms
  - Max response time : 9,652ms (outlier significatif)
  - Throughput : 19.5 req/s (Aggregate) / 18.0 req/s (Summary)
  - **Observation** : Latence élevée avec des pics à 9.6 secondes - possible problème de base de données

- **GET /items (50%)** : **Performance similaire à Init**
  - 795 échantillons (Aggregate) / 422 (Summary)
  - Temps de réponse moyen : 591ms (Summary) / 844ms (Aggregate)
  - Médiane : 828ms, P95 : 1,302ms, P99 : 4,616ms
  - Max response time : 6,283ms
  - Throughput : 9.7 req/s (Aggregate) / 8.9 req/s (Summary)
  - **Observation** : Comportement similaire à Init GET, latence élevée

- **GET /items?categoryId... (20%)** : **Performance similaire, requête avec filtre**
  - 315 échantillons (Aggregate) / 164 (Summary)
  - Temps de réponse moyen : 595ms (Summary) / 855ms (Aggregate)
  - Médiane : 832ms, P95 : 1,483ms, P99 : 4,953ms
  - Max response time : 5,722ms
  - Throughput : 3.9 req/s (Aggregate) / 3.5 req/s (Summary)
  - **Observation** : Le filtre par categoryId n'améliore pas la performance - possible manque d'index

- **GET /categories/{id} (20%)** : **Meilleure performance que Items**
  - 310 échantillons (Aggregate) / 161 (Summary)
  - Temps de réponse moyen : 430ms (Summary) / 692ms (Aggregate)
  - Médiane : 774ms, P95 : 1,099ms, P99 : 1,272ms
  - Max response time : 2,021ms
  - Throughput : 3.8 req/s (Aggregate) / 3.5 req/s (Summary)
  - **Observation** : Performance meilleure que Items, P99 raisonnable à 1,272ms

- **GET /categories (10%)** : **Meilleure performance globale**
  - 158 échantillons (Aggregate) / 82 (Summary)
  - Temps de réponse moyen : 337ms (Summary) / 609ms (Aggregate)
  - Médiane : 709ms, P95 : 1,046ms, P99 : 1,146ms
  - Min response time : 13ms (le plus rapide)
  - Max response time : 1,546ms
  - Throughput : 2.0 req/s (Aggregate) / 1.8 req/s (Summary)
  - **Observation** : Performance la plus stable, P99 à 1,146ms - excellent

### Performance Générale

- **Throughput Total** : 38.5 req/s (Aggregate) / 35.2 req/s (Summary)
  - GET uniquement (0% POST)
  - Distribution :
    - Init GET Items : ~19.5 req/s (50% du throughput)
    - GET /items : ~9.7 req/s (25% du throughput)
    - GET avec filtre/categories : ~9.3 req/s (25% du throughput)

- **Latence** :
  - Médiane globale : 826ms (Aggregate) / 567ms (Summary)
  - P95 : 1,260ms (Aggregate)
  - P99 : 4,780ms (Aggregate)
  - Max : 9,652ms (outlier sur Init GET Items)
  - **Observation** : Latence globale élevée, surtout sur les requêtes Items

- **Memory Usage** :
  - Heap très bas (1.10%) - Pas de problème mémoire
  - Non-Heap élevé (91.3%) - Normal pour Spring Boot, mais à surveiller
  - G1 Eden Space : Pattern en dents de scie (0-40 MB) - GC actif mais normal
  - G1 Old Gen : Croissance lente (44-45.5 MB) - Pas d'accumulation
  - GC pause time faible (0.537ms moyenne, max 0.769ms) - Excellent, pas de problème GC

- **CPU Usage** :
  - CPU système à 100% - **Goulot d'étranglement majeur**
  - CPU processus : 5.26% moyenne (7.33% max) - Application pas surchargée
  - **Observation** : Le CPU système est saturé, probablement par la base de données ou I/O

- **Threads** :
  - ~72-73 threads actifs pendant le test
  - Stable avec quelques fluctuations mineures

- **RPS vs CPU** :
  - RPS Total : 44.8 req/s (Grafana) vs 38.5 req/s (JMeter Aggregate)
  - Discrepancy probable due aux périodes de calcul différentes
  - GET uniquement (0 POST) - Cohérent avec le scénario READ-heavy

### Analyse Comparative

- **Items vs Categories** :
  - Items : Latence moyenne ~600-850ms, P99 ~4,600-4,900ms
  - Categories : Latence moyenne ~330-690ms, P99 ~1,146-1,272ms
  - **Categories 3-4x plus rapides** que Items
  - **Recommandation** : Optimiser les requêtes Items (index, jointures, pagination)

- **Requêtes avec filtre vs sans filtre** :
  - GET /items : 591ms moyenne
  - GET /items?categoryId : 595ms moyenne
  - **Le filtre n'améliore pas la performance** - Possible manque d'index sur categoryId

- **Outliers** :
  - Max response time : 9,652ms sur Init GET Items
  - Plusieurs valeurs P99 > 4,000ms
  - **Recommandation** : Investiguer les causes des pics de latence (timeouts DB, locks, slow queries)

### Recommandations

1. **URGENT - Optimiser les requêtes Items** :
   - Latence très élevée (P99 à 4,900ms)
   - Vérifier les index sur les colonnes utilisées dans les WHERE/JOIN
   - Optimiser les requêtes avec EXPLAIN
   - Considérer la pagination plus agressive si applicable

2. **Optimiser les requêtes avec filtre categoryId** :
   - Le filtre n'apporte pas de gain de performance
   - Vérifier l'existence d'un index sur `category_id` dans la table Items
   - Possible index composite si d'autres filtres sont utilisés

3. **Investiguer les outliers de latence** :
   - Max response time à 9,652ms inacceptable
   - Analyser les logs pour identifier les slow queries
   - Vérifier les timeouts de connexion base de données
   - Possible problème de locks ou de contention

4. **CPU Système à 100%** :
   - Goulot d'étranglement probablement au niveau base de données
   - Vérifier la charge CPU de la base de données
   - Considérer l'optimisation des requêtes (réduction du nombre, mise en cache)
   - Possible problème I/O disque

5. **Considérer la mise en cache** :
   - Scénario READ-heavy idéal pour la mise en cache
   - Les requêtes répétées (GET /items, GET /categories) pourraient bénéficier d'un cache
   - Cache Redis ou Spring Cache pour réduire la charge DB

6. **Monitoring continu** :
   - Non-Heap à 91.3% - Surveiller la métaspace/permgen
   - Threads stables mais nombreux (~72-73) - Vérifier si nécessaire

---

## Conclusion

Le test READ-heavy révèle une **stabilité parfaite avec 0% d'erreurs** sur toutes les opérations. Cependant, la **latence est élevée**, particulièrement sur les requêtes Items (P99 à ~4,900ms). Les requêtes Categories sont 3-4x plus rapides. Le **CPU système à 100%** indique un goulot d'étranglement probablement au niveau base de données. Des optimisations d'index, de requêtes et potentiellement de mise en cache sont recommandées pour améliorer les performances, notamment pour les requêtes Items.
