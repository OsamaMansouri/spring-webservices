# 📊 Résultats - Scénario MIXED (Spring Data REST)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~13:46:30
- **Heure fin** : ~13:51:15
- **Threads** : 60
- **Ramp-up** : 60s
- **Durée** : 480s (8 min)

## Mix de Requêtes

- 40% : GET /api-data-rest/items
- 20% : POST /api-data-rest/items
- 10% : PUT /api-data-rest/items/{id}
- 10% : DELETE /api-data-rest/items/{id}
- 10% : POST /api-data-rest/categories
- 10% : PUT /api-data-rest/categories/{id}

---

## Résultats JMeter

### Summary Report

| Endpoint                    | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| --------------------------- | --------- | ------------ | -------- | -------- | --------- | ------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items            | 8938      | 137          | 10       | 695      | 91.10     | 0.00%   | 91.9/sec         | 996.12          | 20.27       | 11104.8    |
| Init - GET Categories       | 8920      | 123          | 4        | 837      | 91.01     | 0.00%   | 91.7/sec         | 929.20          | 20.59       | 10378.7    |
| GET /items (40%)            | 3564      | 138          | 9        | 866      | 95.26     | 0.00%   | 36.7/sec         | 397.47          | 8.18        | 11104.7    |
| POST /items (20%)           | 1783      | 99           | 3        | 603      | 79.41     | 0.00%   | 18.3/sec         | 14.08           | 6.06        | 786.1      |
| PUT /items/{id} (10%)       | 891       | 102          | 3        | 515      | 79.04     | **100.00%** | 9.2/sec         | 5.04            | 3.15        | 562.7      |
| DELETE /items/{id} (10%)    | 888       | 99           | 1        | 534      | 80.80     | **100.00%** | 9.2/sec         | 1.88            | 1.94        | 209.8      |
| POST /categories (10%)      | 888       | 96           | 2        | 433      | 75.97     | 0.00%   | 9.1/sec          | 6.83            | 2.52        | 764.2      |
| PUT /categories/{id} (10%)  | 885       | 102          | 3        | 626      | 81.46     | **62.03%** | 9.1/sec         | 6.67            | 2.63        | 747.1      |
| **TOTAL**                   | **26757** | **125**      | **1**    | **866**  | **90.55** | **8.70%** | **274.9/sec**    | **2356.57**     | **65.28**   | **8776.7** |

### Aggregate Report

| Endpoint                    | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Min (ms) | Maximum (ms) | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| --------------------------- | --------- | ------------ | -------- | -------- | -------- | -------- | ------------ | ------- | ---------------- | --------------- | ----------- |
| Init - GET Items            | 22390     | 179          | 277      | 332      | 465      | 10       | 1015         | 0.00%   | 88.1/sec         | 955.09          | 19.44       |
| Init - GET Categories       | 22374     | 166          | 265      | 320      | 454      | 4        | 1163         | 0.00%   | 88.0/sec         | 892.10          | 19.77       |
| GET /items (40%)            | 8940      | 181          | 278      | 331      | 473      | 9        | 938          | 0.00%   | 35.2/sec         | 381.43          | 7.85        |
| POST /items (20%)           | 4473      | 145          | 211      | 269      | 405      | 3        | 871          | 0.00%   | 17.6/sec         | 13.51           | 5.82        |
| PUT /items/{id} (10%)       | 2234      | 147          | 218      | 278      | 432      | 3        | 661          | **100.00%** | 8.8/sec         | 4.83            | 3.02        |
| DELETE /items/{id} (10%)    | 2233      | 141          | 212      | 272      | 397      | 1        | 662          | **100.00%** | 8.8/sec         | 1.80            | 1.86        |
| POST /categories (10%)      | 2232      | 142          | 205      | 267      | 380      | 2        | 855          | 0.00%   | 8.8/sec          | 6.56            | 2.42        |
| PUT /categories/{id} (10%)  | 2231      | 148          | 217      | 265      | 388      | 3        | 663          | **74.90%** | 8.8/sec         | 6.38            | 2.53        |
| **TOTAL**                   | **67107** | **169**      | **264**  | **319**  | **449**  | **1**    | **1163**     | **9.15%** | **264.0/sec**    | **2261.52**     | **62.69**   |

---

## Métriques Grafana

**Période du test** : 13:46:30 - 13:51:15 (≈5 minutes)

| Métrique                        | Valeur moyenne | Valeur pic | Min      | Max      | Last      |
| ------------------------------- | -------------- | ---------- | -------- | -------- | --------- |
| **CPU Usage - System (%)**      | 100.0          | 100.0      | 14.5     | 100.0    | 100.0     |
| **CPU Usage - Process (%)**     | 45.2           | 46.4       | 0.664    | 46.4     | 45.2      |
| **Heap Used (%)**               | 1.08           | 1.08       | 1.08     | 1.08     | 1.08      |
| **Non-Heap Used (%)**           | 90.4           | 90.4       | 90.4     | 90.4     | 90.4      |
| **GC Pause Time (ms)**          | 1.18           | 3.45       | 0        | 3.45     | 3.45      |
| **Threads actifs**              | 20-70          | 70+        | 20       | 70+      | 70+       |
| **CPU Core Size**               | 20             | 20         | 20       | 20       | 20        |
| **Threads vs CPU Cores**        | N/A            | N/A        | 20/20    | 70+/20   | 70+/20    |
| **G1 Eden Space (MB)**          | 10-30          | 30         | 10       | 30       | 10        |
| **G1 Old Gen (MB)**             | 45-49          | 49         | 45       | 49       | 49        |
| **Requests Per Second (Total)** | 61.1 req/s     | 177 req/s  | 0        | 177      | 177       |
| **Requests Per Second (GET)**   | 48.9 req/s     | 142 req/s  | 0        | 142      | 142       |
| **Requests Per Second (POST)**  | 6.09 req/s     | 17.7 req/s | 0        | 17.7     | 17.7      |
| **HTTP Error Rate (%)**         | 8.98           | 8.98       | 8.98     | 8.98     | 8.98      |
| **Uptime (hours)**              | 1.79           | 1.79       | 1.79     | 1.79     | 1.79      |

---

## Observations

- **Critical Issues - PUT/DELETE Operations** : 
  - **PUT /items/{id}**: 100% error rate (2234 échantillons). Tous les PUT requests ont échoué. Cause probable: format HAL JSON incorrect pour la catégorie ou problème d'extraction d'itemId depuis la réponse HAL.
  - **DELETE /items/{id}**: 100% error rate (2233 échantillons). Tous les DELETE requests ont échoué. Cause probable: itemId non valide ou format d'URL incorrect.
  - **PUT /categories/{id}**: 74.90% error rate (2231 échantillons). La majorité des PUT categories ont échoué, probablement dû à des codes de catégorie dupliqués ou existingCategoryId non valide.

- **Mixed Workload Performance** : 
  - Les opérations GET et POST fonctionnent parfaitement avec 0% d'erreur
  - Throughput global de 264.0 req/s est excellent
  - Les temps de réponse moyens sont acceptables (125ms global, 99-179ms par endpoint)
  - Le taux d'erreur global de 9.15% est entièrement dû aux échecs de PUT/DELETE

- **CRUD Operations Balance** : 
  - GET operations (40%): Excellentes performances avec 0% d'erreur, latence moyenne 138-179ms
  - POST operations (20% + 10%): 100% de succès avec latence moyenne 96-137ms
  - PUT operations (10% + 10%): Échecs complets pour items, 74.90% d'échec pour categories
  - DELETE operations (10%): Échecs complets avec 100% d'erreur

- **HAL JSON Format Impact** : 
  - Le format HAL JSON fonctionne correctement pour GET et POST
  - Les problèmes surviennent uniquement lors des opérations PUT/DELETE qui nécessitent des IDs extraits du format HAL
  - L'extraction d'itemId depuis `_embedded.items` semble ne pas fonctionner correctement

- **Memory Usage** : 
  - Heap utilisé très faible (1.08%), indiquant une bonne gestion mémoire
  - Non-Heap utilisé très élevé (90.4%), typique des applications Java avec métadonnées de classes
  - G1 Eden Space stable (10-30 MB) avec GC efficace (pause moyenne 1.18ms)
  - G1 Old Gen augmente progressivement (45-49 MB), normal sous charge

- **CPU and Threads** : 
  - System CPU saturé à 100%, indiquant une charge système maximale
  - Process CPU à 45.2%, suggérant une utilisation efficace mais intensive
  - Threads augmentent de 20 à 70+ pendant le test, dépassant les CPU cores (20), ce qui peut expliquer la saturation CPU

- **Overall System Stability** : 
  - Malgré les erreurs PUT/DELETE, le système reste stable avec un uptime de 1.79 heures
  - Les requêtes GET/POST continuent de fonctionner correctement même sous charge
  - La latence reste acceptable malgré la saturation CPU

- **Root Cause Analysis** : 
  - Les erreurs 100% pour PUT/DELETE items suggèrent que `${itemId}` n'est pas correctement extrait ou est invalide
  - Le regex extractor pour itemId doit être ajusté pour mieux gérer le format HAL JSON de Spring Data REST
  - Pour PUT categories, les erreurs 74.90% peuvent être dues à des codes de catégorie non uniques ou existingCategoryId invalide

- **Comparison with REST Controllers** : 
  - Contrairement aux REST Controllers qui fonctionnent correctement, Spring Data REST présente des problèmes avec PUT/DELETE
  - Le format HAL JSON ajoute une complexité supplémentaire pour l'extraction d'IDs nécessaires aux opérations PUT/DELETE
  - Les performances GET/POST sont comparables, mais Spring Data REST nécessite des ajustements pour PUT/DELETE

