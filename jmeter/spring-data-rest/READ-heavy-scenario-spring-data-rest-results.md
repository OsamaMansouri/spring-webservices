# 📊 Résultats - Scénario READ-heavy (Spring Data REST)

## Configuration du Test

- **Date** : 2025-11-03
- **Heure début** : ~13:58:45
- **Heure fin** : ~14:03:30
- **Threads** : 60
- **Ramp-up** : 60s
- **Durée** : 480s (8 min)

## Mix de Requêtes

- 50% : GET /api-data-rest/items
- 20% : GET /api-data-rest/items/search/byCategory?categoryId={id}
- 20% : GET /api-data-rest/categories/{id}
- 10% : GET /api-data-rest/categories

---

## Résultats JMeter

### Summary Report

| Endpoint                      | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
| ----------------------------- | --------- | ------------ | -------- | -------- | --------- | ------- | ---------------- | --------------- | ----------- | ---------- |
| Init - GET Items              | 278       | 650          | 11       | 4205     | 903.37    | 0.00%   | 7.0/sec          | 76.20           | 1.61        | 11105.0    |
| GET /items (50%)              | 132       | 656          | 13       | 4161     | 917.74    | 0.00%   | 3.3/sec          | 36.30           | 0.77        | 11105.0    |
| GET /items/search/byCategory..| 52        | 618          | 17       | 4182     | 919.24    | 0.00%   | 1.3/sec          | 0.71            | 0.34        | 553.0      |
| GET /categories (10%)         | 25        | 696          | 7        | 3079     | 933.73    | 0.00%   | 39.9/min         | 6.73            | 0.16        | 10379.0    |
| GET /categories/{id} (20%)    | 40        | 6947         | 3632     | 10744    | 2406.16   | 0.00%   | 1.0/sec          | 5627.82         | 0.25        | 5596486.0  |
| **TOTAL**                     | **527**   | **1129**     | **7**    | **10744** | **1996.72** | **0.00%** | **13.3/sec**    | **5645.04**     | **3.11**    | **433967.2** |

### Aggregate Report

| Endpoint                      | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Min (ms) | Maximum (ms) | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec |
| ----------------------------- | --------- | ------------ | -------- | -------- | -------- | -------- | ------------ | ------- | ---------------- | --------------- | ----------- |
| Init - GET Items              | 395       | 720          | 3495     | 4015     | 5176     | 11       | 5226         | 0.00%   | 6.6/sec          | 71.38           | 1.50        |
| GET /items (50%)              | 186       | 799          | 3747     | 4075     | 5235     | 13       | 5253         | 0.00%   | 3.1/sec          | 33.71           | 0.71        |
| GET /items/search/byCategory..| 72        | 710          | 3304     | 4056     | 5255     | 17       | 5280         | 0.00%   | 1.2/sec          | 0.65            | 0.31        |
| GET /categories (10%)         | 35        | 834          | 3295     | 4047     | 5231     | 7        | 5231         | 0.00%   | 36.1/min         | 6.09            | 0.14        |
| GET /categories/{id} (20%)    | 60        | 8817         | 13149    | 14429    | 14668    | 3632     | 14920        | 0.00%   | 1.0/sec          | 5507.82         | 0.24        |
| **TOTAL**                     | **748**   | **858**      | **4184** | **8117** | **12724** | **7**    | **14920**    | **0.00%** | **12.5/sec**    | **5576.06**     | **2.91**    |

---

## Métriques Grafana

**Période du test** : 13:58:45 - 14:03:30 (≈5 minutes)

| Métrique                        | Valeur moyenne | Valeur pic | Min      | Max      | Last     |
| ------------------------------- | -------------- | ---------- | -------- | -------- | -------- |
| **CPU Usage - System (%)**      | 100.0          | 100.0      | 8.59     | 100.0    | 100.0    |
| **CPU Usage - Process (%)**     | 64.6           | 79.1       | 0.677    | 79.1     | 64.6     |
| **Heap Used (%)**               | 24.6           | 24.6       | 24.6     | 24.6     | 24.6     |
| **Non-Heap Used (%)**           | 92.1           | 92.1       | 92.1     | 92.1     | 92.1     |
| **GC Pause Time (ms)**          | 3.48           | 13.6       | 0        | 13.6     | 13.6     |
| **Threads actifs**              | 20-70          | 70+        | 20       | 70+      | 70+      |
| **CPU Core Size**               | 20             | 20         | 20       | 20       | 20       |
| **Threads vs CPU Cores**        | N/A            | N/A        | 20/20    | 70+/20   | 70+/20   |
| **G1 Eden Space (MB)**          | 0-2000         | 2000       | 0        | 2000     | 1500     |
| **G1 Old Gen (MB)**             | 55-65          | 65         | 55       | 65       | 65       |
| **Requests Per Second (Total)** | Variable      | 215 req/s  | 2.77     | 215      | 2.77     |
| **Requests Per Second (GET)**   | Variable      | 172 req/s  | 2.77     | 172      | 2.77     |
| **Requests Per Second (POST)**  | Variable      | 21.5 req/s | 0        | 21.5     | 0        |
| **HTTP Error Rate (%)**         | 0.00           | 0.00       | 0.00     | 0.00     | 0.00     |
| **Uptime (hours)**              | 2.00           | 2.00       | 2.00     | 2.00     | 2.00     |

---

## Observations

- **Read-Heavy Workload Performance** : 
  - Toutes les requêtes READ ont 0% d'erreur, démontrant la stabilité de Spring Data REST pour les opérations de lecture
  - Throughput global de 12.5-13.3 req/s est modéré mais stable
  - La latence moyenne globale de 1129ms (Summary) / 1886ms (Aggregate) est élevée, principalement due à GET /categories/{id} qui a une latence moyenne exceptionnelle de 6947ms
  - Les requêtes GET /items et GET /items/search/byCategory montrent des performances similaires (618-656ms moyenne), indiquant que les filtres n'ajoutent pas de surcharge significative

- **Critical Issue - GET /categories/{id} Performance** : 
  - **GET /categories/{id}**: Latence extrêmement élevée avec une moyenne de 6947ms (6.9 secondes!) et un maximum de 10744ms (10.7 secondes)
  - Cette requête représente seulement 20% du mix mais domine les statistiques globales
  - La latence médiane de 8817ms et les percentiles élevés (90th: 13149ms, 95th: 14429ms, 99th: 14668ms) indiquent que presque toutes les requêtes sont lentes
  - Taille de réponse énorme: 5596486.0 bytes (≈5.6 MB) en moyenne, expliquant la latence élevée
  - Throughput de 5627.82 KB/sec reçu, indiquant que cette requête transfère des volumes massifs de données (probablement avec relations HAL JSON imbriquées)
  - **Root Cause**: Cette requête charge probablement toutes les relations `items` d'une catégorie via HAL JSON, créant une réponse gigantesque avec des milliers d'items imbriqués

- **HAL JSON Format Impact** : 
  - Le format HAL JSON fonctionne correctement pour les requêtes simples (GET /items, GET /categories)
  - Les relations HAL JSON créent des réponses très volumineuses pour GET /categories/{id}, entraînant une latence élevée
  - La taille moyenne de réponse pour GET /categories/{id} (5.6 MB) est 540x plus grande que GET /items/search/byCategory (553 bytes)
  - Les requêtes GET /items retournent 11105 bytes en moyenne, ce qui est raisonnable

- **Query Optimization** : 
  - GET /items/search/byCategory montre des performances similaires à GET /items (618ms vs 656ms), indiquant que les requêtes JOIN sont bien optimisées
  - La latence médiane pour les requêtes items (710-799ms) est acceptable malgré la variabilité élevée (std dev 917-933ms)
  - Les requêtes categories simples (GET /categories) ont une latence similaire (696ms moyenne, 834ms médiane)

- **Memory Usage** : 
  - Heap utilisé à 24.6%, indiquant une utilisation mémoire modérée malgré les grandes réponses
  - Non-Heap utilisé très élevé à 92.1%, typique des applications Java avec beaucoup de métadonnées de classes
  - G1 Eden Space montre une utilisation variable (0-2000 MB), avec des pics à 2 GB, indiquant de grandes allocations pour les grandes réponses HAL JSON
  - G1 Old Gen augmente progressivement de 55 MB à 65 MB, montrant une accumulation d'objets de longue durée
  - GC pause time moyen de 3.48ms (pic 13.6ms) est acceptable mais augmente avec la charge

- **CPU and Threads** : 
  - System CPU saturé à 100% pendant une grande partie du test, indiquant une charge système maximale
  - Process CPU à 64.6% en moyenne (pic 79.1%), montrant une utilisation CPU intensive mais non saturée par le processus
  - Threads fluctuent de 20 à 70+, avec une augmentation significative vers la fin du test
  - Ratio threads/CPU cores passe de 20/20 (1:1) à 70+/20 (3.5:1), expliquant la saturation CPU système

- **Database Load** : 
  - Le nombre élevé de requêtes GET /categories/{id} avec réponses massives suggère une charge importante sur la base de données
  - Chaque GET /categories/{id} charge probablement tous les items associés, créant des requêtes SQL complexes avec de nombreux JOINs
  - La taille de réponse de 5.6 MB par requête indique que chaque catégorie retourne des centaines ou milliers d'items avec leurs relations HAL JSON complètes

- **RPS Pattern Analysis** : 
  - RPS démarre élevé (≈90-100 req/s) puis diminue progressivement à environ 50 req/s, puis chute drastiquement à près de 0 req/s
  - Cette diminution pourrait être due à la saturation CPU et aux latences élevées de GET /categories/{id} qui ralentissent l'ensemble du système
  - RPS final de 2.77 req/s reflète la dégradation des performances due à la charge CPU et aux grandes réponses

- **Overall System Stability** : 
  - Malgré les latences élevées, le système reste stable avec 0% d'erreur HTTP
  - Uptime de 2.00 heures confirme la stabilité à long terme
  - Les requêtes continuent de se compléter même si elles sont lentes, indiquant qu'il n'y a pas de timeouts

- **Comparison with REST Controllers** : 
  - Spring Data REST montre des performances similaires pour les requêtes simples (GET /items, GET /categories)
  - Le problème majeur est GET /categories/{id} qui charge automatiquement toutes les relations via HAL JSON, contrairement aux REST Controllers où cette relation serait probablement paginée ou lazy-loaded
  - Les REST Controllers offrent plus de contrôle sur ce qui est retourné, permettant d'éviter les réponses massives
  - Pour les scénarios read-heavy, Spring Data REST nécessite une configuration spécifique pour éviter le chargement automatique de toutes les relations imbriquées

- **Recommendations** : 
  - Configurer Spring Data REST pour éviter le chargement automatique de toutes les relations items lors de GET /categories/{id}
  - Implémenter de la pagination ou utiliser des projections pour limiter les données retournées
  - Considérer l'utilisation de `@RestResource(exported = false)` pour certaines relations ou configurer des projections personnalisées
  - Monitorer et optimiser les requêtes SQL générées pour GET /categories/{id} avec relations

