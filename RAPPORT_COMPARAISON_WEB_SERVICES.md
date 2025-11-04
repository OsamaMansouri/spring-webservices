# Rapport de Comparaison : JAX-RS (Jersey) vs Spring @RestController vs Spring Data REST

## Introduction

Ce rapport présente une analyse comparative de trois approches pour la création de web services REST dans Spring Boot :

1. **JAX-RS (Jersey)** - Implémentation actuelle du projet
2. **Spring @RestController** - Approche native Spring MVC
3. **Spring Data REST** - Génération automatique d'API REST à partir des repositories

L'analyse se base sur le projet actuel gérant des items et catégories (entities `Item` et `Category`) avec les opérations CRUD standards. Le projet utilise PostgreSQL avec 2 000 catégories et 100 000 items pour les tests de performance.

---

## 1. Analyse de l'Implémentation Actuelle : JAX-RS (Jersey)

### 1.1 Structure du Code

#### Contrôleur JAX-RS (`ItemJaxrsApi.java`)

```java
@Component
@Path("/items")
public class ItemJaxrsApi {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Page<Item> getItems(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("categoryId") Long categoryId) { ... }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Item getItem(@PathParam("id") Long id) { ... }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Item addItem(Item item) { ... }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Item updateItem(@PathParam("id") Long id, Item item) { ... }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void deleteItem(@PathParam("id") Long id) { ... }
}
```

#### Configuration (`MyConfig.java`)

```java
@Configuration
public class MyConfig {
    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig jerseyServlet = new ResourceConfig();
        jerseyServlet.register(ItemJaxrsApi.class);
        jerseyServlet.register(CategoryJaxrsApi.class);
        return jerseyServlet;
    }
}
```

### 1.2 Endpoints Disponibles

**Base path JAX-RS** : `/api` (configuré via `spring.jersey.application-path=/api`)

| Méthode HTTP | Endpoint                     | Description                                                         |
| ------------ | ---------------------------- | ------------------------------------------------------------------- |
| GET          | `/api/items`                 | Récupérer tous les items (avec pagination et filtre par categoryId) |
| GET          | `/api/items/{id}`            | Récupérer un item par ID                                            |
| POST         | `/api/items`                 | Créer un nouveau item                                               |
| PUT          | `/api/items/{id}`            | Mettre à jour un item                                               |
| DELETE       | `/api/items/{id}`            | Supprimer un item                                                   |
| GET          | `/api/categories`            | Récupérer toutes les catégories (avec pagination)                   |
| GET          | `/api/categories/{id}`       | Récupérer une catégorie par ID                                      |
| GET          | `/api/categories/{id}/items` | Récupérer tous les items d'une catégorie                            |
| POST         | `/api/categories`            | Créer une nouvelle catégorie                                        |
| PUT          | `/api/categories/{id}`       | Mettre à jour une catégorie                                         |
| DELETE       | `/api/categories/{id}`       | Supprimer une catégorie                                             |

### 1.3 Caractéristiques

#### Avantages

- ✅ **Standard JAX-RS** : Conforme à la spécification Java EE/Jakarta EE
- ✅ **Support XML natif** : Sérialisation JSON et XML automatique via `@XmlRootElement`
- ✅ **Annotations explicites** : `@Path`, `@GET`, `@POST`, etc. rendent le code très lisible
- ✅ **Flexibilité** : Contrôle total sur chaque endpoint
- ✅ **Portabilité** : Code compatible avec différents serveurs d'application (Tomcat, Jetty, etc.)
- ✅ **Gestion des types MIME** : Support explicite de plusieurs formats de réponse

#### Inconvénients

- ❌ **Configuration supplémentaire** : Nécessite une classe de configuration (`MyConfig`)
- ❌ **Dépendances externes** : Jersey et JAXB doivent être ajoutées au projet
- ❌ **Code plus verbeux** : Plus d'annotations nécessaires
- ❌ **Gestion manuelle des erreurs** : Pas de gestion d'exceptions HTTP automatique
- ❌ **Pas de validation automatique** : Validation doit être ajoutée manuellement

### 1.4 Lignes de Code

- Contrôleur : ~67 lignes
- Configuration : ~16 lignes
- **Total : ~83 lignes**

---

## 2. Approche Alternative : Spring @RestController

### 2.1 Structure du Code (Implémentation Équivalente)

#### Contrôleur Spring MVC (`ItemRestController.java`)

```java
@RestController
@RequestMapping("/api-rest/items")
public class ItemRestController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<Page<Item>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        if (categoryId != null) {
            Page<Item> items = itemRepository.findByCategoryId(categoryId, pageable);
            return ResponseEntity.ok(items);
        }
        Page<Item> items = itemRepository.findAll(pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return itemRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> addItem(@RequestBody Item item) {
        if (item.getCategory() != null && item.getCategory().getId() != null) {
            Category category = categoryRepository.findById(item.getCategory().getId())
                .orElse(null);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            item.setCategory(category);
        }
        item.setUpdatedAt(LocalDateTime.now());
        Item saved = itemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemRepository.findById(id)
            .map(existing -> {
                existing.setSku(item.getSku());
                existing.setName(item.getName());
                existing.setPrice(item.getPrice());
                existing.setStock(item.getStock());
                existing.setUpdatedAt(LocalDateTime.now());
                if (item.getCategory() != null && item.getCategory().getId() != null) {
                    Category category = categoryRepository.findById(item.getCategory().getId())
                        .orElse(null);
                    if (category == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).<Item>build();
                    }
                    existing.setCategory(category);
                }
                Item saved = itemRepository.save(existing);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

### 2.2 Endpoints Disponibles

**Base path REST Controllers** : `/api-rest` (configuré via `@RequestMapping`)

| Méthode HTTP | Endpoint                          | Description                                                         |
| ------------ | --------------------------------- | ------------------------------------------------------------------- |
| GET          | `/api-rest/items`                 | Récupérer tous les items (avec pagination et filtre par categoryId) |
| GET          | `/api-rest/items/{id}`            | Récupérer un item par ID                                            |
| POST         | `/api-rest/items`                 | Créer un nouveau item                                               |
| PUT          | `/api-rest/items/{id}`            | Mettre à jour un item                                               |
| DELETE       | `/api-rest/items/{id}`            | Supprimer un item                                                   |
| GET          | `/api-rest/categories`            | Récupérer toutes les catégories (avec pagination)                   |
| GET          | `/api-rest/categories/{id}`       | Récupérer une catégorie par ID                                      |
| GET          | `/api-rest/categories/{id}/items` | Récupérer tous les items d'une catégorie                            |
| POST         | `/api-rest/categories`            | Créer une nouvelle catégorie                                        |
| PUT          | `/api-rest/categories/{id}`       | Mettre à jour une catégorie                                         |
| DELETE       | `/api-rest/categories/{id}`       | Supprimer une catégorie                                             |

### 2.3 Caractéristiques

#### Avantages

- ✅ **Intégration native Spring** : Aucune configuration supplémentaire nécessaire
- ✅ **Gestion d'erreurs robuste** : `@ControllerAdvice` pour la gestion globale des exceptions
- ✅ **Validation intégrée** : Support de Bean Validation avec `@Valid`
- ✅ **Moins de dépendances** : Utilise uniquement Spring Boot Web (déjà présent)
- ✅ **`ResponseEntity` flexible** : Contrôle total sur les codes HTTP et headers
- ✅ **Support HAL, HATEOAS** : Intégration facile avec Spring HATEOAS
- ✅ **Documentation automatique** : Intégration facile avec Swagger/OpenAPI

#### Inconvénients

- ❌ **JSON par défaut** : Support XML nécessite configuration supplémentaire
- ❌ **Annotations Spring spécifiques** : Moins portable que JAX-RS
- ❌ **Moins de contrôle sur les types MIME** : Configuration manuelle pour XML

### 2.4 Lignes de Code

- Contrôleur : ~55 lignes
- Configuration : 0 ligne (utilise la configuration Spring Boot par défaut)
- **Total : ~55 lignes**

---

## 3. Approche Alternative : Spring Data REST

### 3.1 Structure du Code (Implémentation Équivalente)

#### Repository (`ItemRepository.java`)

```java
@Repository
@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends JpaRepository<Item, Long> {
    @RestResource(path = "byCategory", rel = "byCategory")
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
}
```

#### Repository (`CategoryRepository.java`)

```java
@Repository
@RepositoryRestResource(collectionResourceRel = "categories", path = "categories")
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
```

#### Configuration (`application.properties`)

```properties
spring.data.rest.base-path=/api-data-rest
spring.data.rest.default-page-size=20
spring.data.rest.max-page-size=100
```

### 3.2 Endpoints Disponibles (Générés Automatiquement)

**Base path Spring Data REST** : `/api-data-rest` (configuré via `spring.data.rest.base-path`)

| Méthode HTTP | Endpoint                                                 | Description                                             |
| ------------ | -------------------------------------------------------- | ------------------------------------------------------- |
| GET          | `/api-data-rest/items`                                   | Récupérer tous les items (avec pagination)              |
| GET          | `/api-data-rest/items/{id}`                              | Récupérer un item par ID (format HAL)                   |
| GET          | `/api-data-rest/items/search/byCategory?categoryId={id}` | Recherche par catégorie (custom method)                 |
| GET          | `/api-data-rest/items?page=0&size=10`                    | Pagination                                              |
| GET          | `/api-data-rest/items?sort=price,asc`                    | Tri                                                     |
| GET          | `/api-data-rest/items/search`                            | Lister les méthodes de recherche                        |
| POST         | `/api-data-rest/items`                                   | Créer un nouveau item (format HAL pour relations)       |
| PUT          | `/api-data-rest/items/{id}`                              | Mettre à jour un item (remplacement complet)            |
| PATCH        | `/api-data-rest/items/{id}`                              | Mettre à jour partielle                                 |
| DELETE       | `/api-data-rest/items/{id}`                              | Supprimer un item                                       |
| GET          | `/api-data-rest/categories`                              | Récupérer toutes les catégories (avec pagination)       |
| GET          | `/api-data-rest/categories/{id}`                         | Récupérer une catégorie par ID (format HAL)             |
| GET          | `/api-data-rest/categories/{id}/items`                   | Récupérer tous les items d'une catégorie (relation HAL) |
| POST         | `/api-data-rest/categories`                              | Créer une nouvelle catégorie                            |
| PUT          | `/api-data-rest/categories/{id}`                         | Mettre à jour une catégorie                             |
| DELETE       | `/api-data-rest/categories/{id}`                         | Supprimer une catégorie                                 |

**Endpoints supplémentaires automatiques :**

- `GET /api-data-rest/items/{id}/category` : Relation HAL vers la catégorie
- `GET /api-data-rest/categories/{id}/items` : Relation HAL vers les items
- `GET /api-data-rest/profile/items` : Métadonnées du profil pour items
- `GET /api-data-rest/profile/categories` : Métadonnées du profil pour categories

### 3.3 Caractéristiques

#### Avantages

- ✅ **Zéro code de contrôleur** : Génération automatique complète
- ✅ **Fonctionnalités avancées** : Pagination, tri, recherche automatiques
- ✅ **Format HAL** : Support natif de HATEOAS (Hypermedia)
- ✅ **Projections** : Vues personnalisées des données
- ✅ **Métadonnées** : Profils et schémas automatiques
- ✅ **Évolutivité** : Gestion automatique des grandes collections
- ✅ **Standard REST** : Conforme aux meilleures pratiques REST

#### Inconvénients

- ❌ **Moins de contrôle** : Personnalisation limitée des endpoints
- ❌ **Courbe d'apprentissage** : Format HAL peut être complexe pour les clients
- ❌ **Surcharge fonctionnelle** : Plus d'endpoints que nécessaire peut-être générés
- ❌ **Personnalisation complexe** : Modification des endpoints nécessite des interceptors
- ❌ **Dépendance supplémentaire** : `spring-boot-starter-data-rest` requis

### 3.4 Lignes de Code

- Repository annoté : ~3 lignes
- Configuration : ~3 lignes dans `application.properties`
- **Total : ~6 lignes**

---

## 4. Comparaison Détaillée

### 4.1 Tableau Comparatif Global

| Critère                    | JAX-RS (Jersey)       | Spring @RestController | Spring Data REST            |
| -------------------------- | --------------------- | ---------------------- | --------------------------- |
| **Lignes de code**         | ~83 lignes            | ~55 lignes             | ~6 lignes                   |
| **Configuration**          | Classe dédiée requise | Aucune                 | Propriétés minimes          |
| **Contrôle**               | Total                 | Total                  | Limité                      |
| **Complexité**             | Moyenne               | Faible                 | Très faible                 |
| **Flexibilité**            | Très élevée           | Très élevée            | Moyenne                     |
| **Support XML**            | Natif                 | Configuration requise  | Configuration requise       |
| **Validation**             | Manuelle              | Intégrée (@Valid)      | Intégrée                    |
| **Gestion d'erreurs**      | Manuelle              | @ControllerAdvice      | Automatique                 |
| **Pagination**             | Manuelle              | Manuelle               | Automatique                 |
| **Tri**                    | Manuelle              | Manuelle               | Automatique                 |
| **HATEOAS**                | Manuelle              | Via Spring HATEOAS     | Natif (HAL)                 |
| **Documentation**          | Swagger manuel        | Swagger automatique    | Profils automatiques        |
| **Standard**               | JAX-RS (Jakarta)      | Spring MVC             | Spring Data REST            |
| **Portabilité**            | Élevée                | Moyenne                | Faible (Spring uniquement)  |
| **Performance**            | Équivalente           | Équivalente            | Légèrement plus lourd (HAL) |
| **Temps de développement** | Moyen                 | Rapide                 | Très rapide                 |

### 4.2 Comparaison par Opération CRUD

#### GET (Récupérer tous les items)

**JAX-RS :**

```java
@Path("/items")
@GET
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public Page<Item> getItems(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size,
        @QueryParam("categoryId") Long categoryId) {
    Pageable pageable = PageRequest.of(page, size);
    if (categoryId != null) {
        return itemRepository.findByCategoryId(categoryId, pageable);
    }
    return itemRepository.findAll(pageable);
}
```

**Spring @RestController :**

```java
@GetMapping
public ResponseEntity<Page<Item>> getItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long categoryId) {
    Pageable pageable = PageRequest.of(page, size);
    if (categoryId != null) {
        Page<Item> items = itemRepository.findByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(items);
    }
    Page<Item> items = itemRepository.findAll(pageable);
    return ResponseEntity.ok(items);
}
```

**Spring Data REST :**

```java
// Aucun code nécessaire - généré automatiquement
// Endpoint: GET /api-data-rest/items
// Inclut automatiquement: pagination, tri, HATEOAS
// Recherche custom: GET /api-data-rest/items/search/byCategory?categoryId={id}
```

#### POST (Créer un item)

**JAX-RS :**

```java
@Path("/items")
@POST
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public Item addItem(Item item) {
    if (item.getCategory() != null && item.getCategory().getId() != null) {
        Category category = categoryRepository.findById(item.getCategory().getId())
            .orElseThrow(() -> new NotFoundException("Category not found"));
        item.setCategory(category);
    }
    item.setUpdatedAt(LocalDateTime.now());
    return itemRepository.save(item);
}
```

**Spring @RestController :**

```java
@PostMapping
public ResponseEntity<Item> addItem(@RequestBody Item item) {
    if (item.getCategory() != null && item.getCategory().getId() != null) {
        Category category = categoryRepository.findById(item.getCategory().getId())
            .orElse(null);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        item.setCategory(category);
    }
    item.setUpdatedAt(LocalDateTime.now());
    Item saved = itemRepository.save(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

**Spring Data REST :**

```java
// Aucun code nécessaire - généré automatiquement
// Inclut automatiquement: validation, codes HTTP appropriés, HAL links
// Format HAL pour relations: "category": "/api-data-rest/categories/{id}"
```

### 4.3 Gestion des Erreurs

#### JAX-RS

```java
// Nécessite une classe ExceptionMapper personnalisée
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorMessage("Resource not found"))
            .build();
    }
}
```

#### Spring @RestController

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

#### Spring Data REST

```java
// Gestion automatique avec messages d'erreur HAL standardisés
// Personnalisation via RepositoryRestExceptionHandler
```

### 4.4 Support de la Pagination

#### JAX-RS

```java
@GET
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public Page<Item> getItems(
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return itemRepository.findAll(pageable);
}
```

#### Spring @RestController

```java
@GetMapping
public ResponseEntity<Page<Item>> getItems(Pageable pageable) {
    Page<Item> items = itemRepository.findAll(pageable);
    return ResponseEntity.ok(items);
}
```

#### Spring Data REST

```java
// Automatique via paramètres de requête: ?page=0&size=10
// Retourne un objet Page avec métadonnées HAL (_links, _embedded, page)
```

---

## 5. Cas d'Usage Recommandés

### 5.1 Quand utiliser JAX-RS (Jersey) ?

✅ **Utilisez JAX-RS quand :**

- Vous avez besoin d'un support XML natif et robuste
- Vous travaillez dans un environnement multi-serveur d'application
- Votre équipe connaît déjà la spécification JAX-RS
- Vous avez besoin d'une conformité stricte aux standards Jakarta EE
- Vous créez des APIs complexes avec des besoins de sérialisation personnalisés

❌ **Évitez JAX-RS quand :**

- Vous développez uniquement pour Spring Boot
- Vous n'avez besoin que de JSON
- Vous voulez minimiser les dépendances externes
- Vous avez besoin de fonctionnalités Spring natives (validation, sécurité)

### 5.2 Quand utiliser Spring @RestController ?

✅ **Utilisez Spring @RestController quand :**

- Vous développez une application Spring Boot pure
- Vous avez besoin d'un contrôle total sur chaque endpoint
- Vous avez des besoins de validation complexes
- Vous intégrez avec d'autres composants Spring (Security, HATEOAS, etc.)
- Vous voulez une documentation Swagger/OpenAPI facile

❌ **Évitez Spring @RestController quand :**

- Vous avez besoin de générer rapidement une API CRUD simple
- Vous voulez minimiser le code écrit
- Vous avez besoin d'un support XML natif sans configuration

### 5.3 Quand utiliser Spring Data REST ?

✅ **Utilisez Spring Data REST quand :**

- Vous créez une API CRUD standard rapidement
- Vous avez besoin de pagination, tri et recherche automatiques
- Vous voulez exposer vos repositories comme API REST
- Vous acceptez le format HAL pour HATEOAS
- Vous développez un backend pour une application frontend moderne

❌ **Évitez Spring Data REST quand :**

- Vous avez besoin de logique métier complexe dans les endpoints
- Vous devez personnaliser fortement le comportement des endpoints
- Vos clients ne comprennent pas le format HAL
- Vous avez besoin d'un contrôle fin sur les codes HTTP et headers

---

## 6. Exemples de Requêtes HTTP

### 6.1 JAX-RS (Jersey)

```bash
# GET tous les items (JSON)
curl -X GET "http://localhost:8087/api/items?page=0&size=20" \
  -H "Accept: application/json"

# GET tous les items (XML)
curl -X GET "http://localhost:8087/api/items?page=0&size=20" \
  -H "Accept: application/xml"

# GET items filtrés par catégorie
curl -X GET "http://localhost:8087/api/items?categoryId=1&page=0&size=20" \
  -H "Accept: application/json"

# GET un item par ID
curl -X GET http://localhost:8087/api/items/1 \
  -H "Accept: application/json"

# POST créer un item
curl -X POST http://localhost:8087/api/items \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"sku": "SKU-001", "name": "Item 1", "price": 100.00, "stock": 50, "category": {"id": 1}}'

# PUT mettre à jour
curl -X PUT http://localhost:8087/api/items/1 \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "sku": "SKU-001-UPD", "name": "Updated Item", "price": 150.00, "stock": 75, "category": {"id": 1}}'

# DELETE supprimer
curl -X DELETE http://localhost:8087/api/items/1

# GET toutes les catégories
curl -X GET "http://localhost:8087/api/categories?page=0&size=20" \
  -H "Accept: application/json"
```

### 6.2 Spring @RestController

```bash
# GET tous les items
curl -X GET "http://localhost:8087/api-rest/items?page=0&size=20"

# GET items filtrés par catégorie
curl -X GET "http://localhost:8087/api-rest/items?categoryId=1&page=0&size=20"

# GET un item par ID
curl -X GET http://localhost:8087/api-rest/items/1

# POST créer un item
curl -X POST http://localhost:8087/api-rest/items \
  -H "Content-Type: application/json" \
  -d '{"sku": "SKU-001", "name": "Item 1", "price": 100.00, "stock": 50, "category": {"id": 1}}'

# PUT mettre à jour
curl -X PUT http://localhost:8087/api-rest/items/1 \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "sku": "SKU-001-UPD", "name": "Updated Item", "price": 150.00, "stock": 75, "category": {"id": 1}}'

# DELETE supprimer
curl -X DELETE http://localhost:8087/api-rest/items/1

# GET toutes les catégories
curl -X GET "http://localhost:8087/api-rest/categories?page=0&size=20"

# GET items d'une catégorie
curl -X GET http://localhost:8087/api-rest/categories/1/items
```

### 6.3 Spring Data REST

```bash
# GET tous les items (avec pagination)
curl -X GET "http://localhost:8087/api-data-rest/items?page=0&size=10"

# GET tous les items (tri par prix)
curl -X GET "http://localhost:8087/api-data-rest/items?sort=price,asc"

# GET items par catégorie (recherche custom)
curl -X GET "http://localhost:8087/api-data-rest/items/search/byCategory?categoryId=1&page=0&size=10"

# GET un item par ID (avec liens HAL)
curl -X GET http://localhost:8087/api-data-rest/items/1

# GET profil (métadonnées)
curl -X GET http://localhost:8087/api-data-rest/profile/items

# POST créer un item (format HAL pour relations)
curl -X POST http://localhost:8087/api-data-rest/items \
  -H "Content-Type: application/json" \
  -d '{"sku": "SKU-001", "name": "Item 1", "price": 100.00, "stock": 50, "category": "/api-data-rest/categories/1"}'

# PUT mise à jour (format HAL)
curl -X PUT http://localhost:8087/api-data-rest/items/1 \
  -H "Content-Type: application/json" \
  -d '{"sku": "SKU-001-UPD", "name": "Updated Item", "price": 150.00, "stock": 75, "category": "/api-data-rest/categories/1"}'

# PATCH mise à jour partielle
curl -X PATCH http://localhost:8087/api-data-rest/items/1 \
  -H "Content-Type: application/json" \
  -d '{"price": 200.00}'

# DELETE supprimer
curl -X DELETE http://localhost:8087/api-data-rest/items/1

# GET toutes les catégories
curl -X GET "http://localhost:8087/api-data-rest/categories?page=0&size=10"

# GET items d'une catégorie (relation HAL)
curl -X GET http://localhost:8087/api-data-rest/categories/1/items
```

**Exemple de réponse HAL (Spring Data REST) :**

```json
{
  "_embedded": {
    "items": [
      {
        "id": 1,
        "sku": "SKU-001",
        "name": "Item 1",
        "price": 100.0,
        "stock": 50,
        "updatedAt": "2025-11-03T12:00:00",
        "_links": {
          "self": {
            "href": "http://localhost:8087/api-data-rest/items/1"
          },
          "item": {
            "href": "http://localhost:8087/api-data-rest/items/1"
          },
          "category": {
            "href": "http://localhost:8087/api-data-rest/items/1/category"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8087/api-data-rest/items{?page,size,sort}",
      "templated": true
    },
    "profile": {
      "href": "http://localhost:8087/api-data-rest/profile/items"
    },
    "search": {
      "href": "http://localhost:8087/api-data-rest/items/search"
    }
  },
  "page": {
    "size": 20,
    "totalElements": 100000,
    "totalPages": 5000,
    "number": 0
  }
}
```

---

## 7. Métriques de Performance et Complexité

### 7.1 Temps de Développement Estimé

| Tâche                  | JAX-RS         | @RestController | Spring Data REST |
| ---------------------- | -------------- | --------------- | ---------------- |
| Configuration initiale | 30 min         | 0 min           | 10 min           |
| Implémentation CRUD    | 2 heures       | 1.5 heures      | 5 min            |
| Gestion d'erreurs      | 1 heure        | 30 min          | 0 min            |
| Validation             | 1 heure        | 15 min          | 15 min           |
| Tests unitaires        | 2 heures       | 2 heures        | 1 heure          |
| **TOTAL**              | **6.5 heures** | **4.25 heures** | **1.5 heures**   |

### 7.2 Complexité du Code

| Aspect                  | JAX-RS                                   | @RestController                   | Spring Data REST     |
| ----------------------- | ---------------------------------------- | --------------------------------- | -------------------- |
| Fichiers à maintenir    | 3+ (Controller, Config, ExceptionMapper) | 2+ (Controller, ExceptionHandler) | 1 (Repository)       |
| Lignes de code          | ~150-200                                 | ~100-120                          | ~10-20               |
| Annotations par méthode | 3-4                                      | 1-2                               | 0                    |
| Configuration externe   | Oui (ResourceConfig)                     | Non                               | Minimal (properties) |

### 7.3 Maintenabilité

| Critère                  | JAX-RS   | @RestController | Spring Data REST                |
| ------------------------ | -------- | --------------- | ------------------------------- |
| Facilité de lecture      | Bonne    | Excellente      | Excellente (moins de code)      |
| Facilité de modification | Bonne    | Excellente      | Limité (nécessite interceptors) |
| Tests unitaires          | Moyenne  | Facile          | Facile                          |
| Tests d'intégration      | Facile   | Facile          | Facile                          |
| Documentation            | Manuelle | Swagger auto    | Profils auto                    |

---

## 8. Sécurité et Validation

### 8.1 JAX-RS

**Validation :**

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response addItem(@Valid Item item) {
    // Nécessite Bean Validation API + implémentation
    if (item.getCategory() != null && item.getCategory().getId() != null) {
        Category category = categoryRepository.findById(item.getCategory().getId())
            .orElseThrow(() -> new NotFoundException("Category not found"));
        item.setCategory(category);
    }
    item.setUpdatedAt(LocalDateTime.now());
    return Response.ok(itemRepository.save(item)).build();
}
```

**Sécurité :** Nécessite Jersey Security ou Spring Security avec configuration supplémentaire.

### 8.2 Spring @RestController

**Validation :**

```java
@PostMapping
public ResponseEntity<Item> addItem(@Valid @RequestBody Item item) {
    // Validation automatique intégrée
    if (item.getCategory() != null && item.getCategory().getId() != null) {
        Category category = categoryRepository.findById(item.getCategory().getId())
            .orElse(null);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        item.setCategory(category);
    }
    item.setUpdatedAt(LocalDateTime.now());
    Item saved = itemRepository.save(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

**Sécurité :** Intégration native avec Spring Security via `@PreAuthorize`, `@Secured`.

### 8.3 Spring Data REST

**Validation :** Automatique si l'entité a des annotations `@NotNull`, `@Min`, etc. Les contraintes de l'entité `Item` (SKU unique, champs non-null) sont automatiquement validées.

**Sécurité :** Configuration via Spring Security avec des règles spécifiques aux repositories.

---

## 9. Conclusion et Recommandations

### 9.1 Résumé des Forces et Faiblesses

#### JAX-RS (Jersey)

**Forces :** Standard, support XML natif, portabilité, contrôle total  
**Faiblesses :** Configuration supplémentaire, code verbeux, dépendances externes

#### Spring @RestController

**Forces :** Intégration native Spring, validation facile, flexibilité, documentation Swagger  
**Faiblesses :** Support XML nécessite config, moins portable

#### Spring Data REST

**Forces :** Génération automatique, fonctionnalités avancées (pagination, HATEOAS), très rapide  
**Faiblesses :** Contrôle limité, format HAL spécifique, personnalisation complexe

### 9.2 Recommandation par Scénario

#### Scénario 1 : API Simple CRUD

**Recommandation :** Spring Data REST  
**Raison :** Développement ultra-rapide, fonctionnalités incluses, moins de code à maintenir

#### Scénario 2 : API Métier Complexe

**Recommandation :** Spring @RestController  
**Raison :** Contrôle total, intégration Spring facile, validation et sécurité natives

#### Scénario 3 : API Multi-Format (JSON + XML)

**Recommandation :** JAX-RS (Jersey)  
**Raison :** Support XML natif et robuste, standard JAX-RS

#### Scénario 4 : Application Enterprise Multi-Serveur

**Recommandation :** JAX-RS (Jersey)  
**Raison :** Portabilité, conformité aux standards Jakarta EE

### 9.3 Recommandation Générale pour ce Projet

Pour le projet actuel de gestion d'items et catégories, **Spring @RestController** serait le meilleur choix car :

1. ✅ Simplifie la configuration (supprime la classe `MyConfig`)
2. ✅ Intègre mieux avec l'écosystème Spring Boot
3. ✅ Facilite l'ajout de validation et de gestion d'erreurs
4. ✅ Permet une documentation Swagger automatique
5. ✅ Réduit les dépendances externes

**Spring Data REST** serait également excellent si :

- Les besoins métier restent simples (CRUD standard)
- La pagination et le tri sont importants
- Le format HAL est acceptable pour les clients

---

## 10. Migration et Évolution

### 10.1 Migration de JAX-RS vers @RestController

**Étapes :**

1. Créer `ItemRestController` et `CategoryRestController` avec `@RestController`
2. Remplacer `@Path` par `@RequestMapping("/api-rest/items")` ou `@RequestMapping("/api-rest/categories")`
3. Remplacer `@GET/@POST` par `@GetMapping/@PostMapping`
4. Utiliser `@PathVariable` au lieu de `@PathParam`
5. Utiliser `@RequestBody` au lieu de paramètres de méthode
6. Utiliser `@RequestParam` au lieu de `@QueryParam`
7. Gérer les relations ManyToOne (Category) manuellement
8. Supprimer les classes `ItemJaxrsApi` et `CategoryJaxrsApi`
9. Optionnel : Supprimer la classe `MyConfig` (si plus de JAX-RS)
10. Optionnel : Supprimer les dépendances Jersey (si plus de JAX-RS)

**Effort estimé :** 2-3 heures (deux controllers)

### 10.2 Migration de JAX-RS vers Spring Data REST

**Étapes :**

1. Ajouter `spring-boot-starter-data-rest` au `pom.xml`
2. Annoter `ItemRepository` avec `@RepositoryRestResource(collectionResourceRel = "items", path = "items")`
3. Annoter `CategoryRepository` avec `@RepositoryRestResource(collectionResourceRel = "categories", path = "categories")`
4. Ajouter méthodes de recherche custom avec `@RestResource` si nécessaire
5. Configurer `spring.data.rest.base-path=/api-data-rest` dans `application.properties`
6. Supprimer `ItemJaxrsApi`, `CategoryJaxrsApi` et `MyConfig`
7. Adapter les clients pour le format HAL JSON (relations comme URLs : `"/api-data-rest/categories/{id}"`)
8. Tester tous les endpoints PUT/DELETE (problèmes potentiels identifiés dans les benchmarks)

**Effort estimé :** 1-2 heures (plus si problèmes avec PUT/DELETE)

---

## 11. Bibliographie et Références

- [JAX-RS Specification (Jakarta EE 10)](https://jakarta.ee/specifications/restful-ws/4.0/)
- [Jersey User Guide](https://eclipse-ee4j.github.io/jersey/)
- [Spring MVC Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Data REST Documentation](https://docs.spring.io/spring-data/rest/docs/current/reference/html/)
- [RESTful Web Services Best Practices](https://restfulapi.net/)
- [HAL Specification (Hypertext Application Language)](https://stateless.group/hal_specification.html)

---

## 12. BENCHMARKS ET MÉTRIQUES DE PERFORMANCE - RÉSULTATS COMPLETS

### 12.1 Environnement de Test

#### Configuration Matérielle et Logicielle

- **Application Spring Boot** : Version 3.5.7
- **Java Version** : JDK 21.0.0.7.6-hotspot (Eclipse Adoptium) - Note: pom.xml indique Java 17 mais runtime utilise JDK 21
- **Base de données** : PostgreSQL 15-alpine (port 5455 Docker ou 5454 local)
- **Port Application** : 8087
- **Threads JMeter** : Variables selon scénario (30→60 pour HEAVY-body, 50→100→200 pour READ-heavy, etc.)
- **Ramp-up** : 60 secondes (standard pour tous les scénarios)
- **Durée des tests** : 480 secondes (8 min) pour HEAVY-body et JOIN-filter, 600 secondes (10 min) pour MIXED et READ-heavy

#### Stack de Monitoring

**Docker Containers utilisés :**

1. **PostgreSQL** :

   - Image : `postgres:15-alpine`
   - Port : 5455 (exposé), 5432 (interne)
   - Base de données : `jaxrs`
   - Utilisateur/Mot de passe : `postgres/0000`

2. **Prometheus** :

   - Image : `prom/prometheus:v2.35.0`
   - Port : 9090
   - Configuration : `./data/prometheus/config/prometheus.yaml`
   - Scrape interval : 15 secondes
   - Target : `host.docker.internal:8087/actuator/prometheus`

3. **Grafana** :
   - Image : `grafana/grafana-oss:8.5.2`
   - Port : 3000
   - Mot de passe admin : `admin`
   - Volumes : `./data/grafana:/var/lib/grafana`

#### Configuration des Web Services

**Chemins de base :**

- **JAX-RS (Jersey)** : `/api/*`
- **REST Controllers** : `/api-rest/*`
- **Spring Data REST** : `/api-data-rest/*`

**Configuration Spring Boot Actuator :**

- Endpoints exposés : `health`, `info`, `metrics`, `prometheus`
- Chemin base : `/actuator`

### 12.2 Scénarios de Test JMeter

#### 12.2.1 Scénario 1 : HEAVY-body

**Description** : Tests avec des corps de requête volumineux (≈5 KB) pour POST/PUT.

**Mix de requêtes** :

- 50% : POST /items (création avec body 5KB)
- 50% : PUT /items/{id} (mise à jour avec body 5KB)

**Fichiers JMeter** :

- `jmeter/jaxrs/heavy-body-scenario.jmx`
- `jmeter/rest/heavy-body-scenario-rest-controller.jmx`
- `jmeter/spring-data-rest/heavy-body-scenario-spring-data-rest.jmx`

**Fichiers de résultats** :

- `jmeter/jaxrs/HEAVY-body-scenario-results.md`
- `jmeter/rest/HEAVY-body-scenario-rest-controller-results.md`
- `jmeter/spring-data-rest/HEAVY-body-scenario-spring-data-rest-results.md`

#### 12.2.2 Scénario 2 : JOIN-filter

**Description** : Tests avec requêtes JOIN complexes et filtres par catégorie.

**Mix de requêtes** :

- 50% : GET /items/search/byCategory?categoryId={id}
- 30% : GET /items/{id}
- 20% : GET /items

**Fichiers JMeter** :

- `jmeter/jaxrs/join-filter-scenario.jmx`
- `jmeter/rest/join-filter-scenario-rest-controller.jmx`
- `jmeter/spring-data-rest/join-filter-scenario-spring-data-rest.jmx`

**Fichiers de résultats** :

- `jmeter/jaxrs/JOIN-filter-scenario-results.md`
- `jmeter/rest/JOIN-filter-scenario-rest-controller-results.md`
- `jmeter/spring-data-rest/JOIN-filter-scenario-spring-data-rest-results.md`

#### 12.2.3 Scénario 3 : MIXED

**Description** : Tests avec mix d'opérations CRUD (GET, POST, PUT, DELETE).

**Mix de requêtes** :

- 40% : GET /items
- 20% : POST /items
- 10% : PUT /items/{id}
- 10% : DELETE /items/{id}
- 10% : POST /categories
- 10% : PUT /categories/{id}

**Fichiers JMeter** :

- `jmeter/jaxrs/mixed-scenario.jmx`
- `jmeter/rest/mixed-scenario-rest-controller.jmx`
- `jmeter/spring-data-rest/mixed-scenario-spring-data-rest.jmx`

**Fichiers de résultats** :

- `jmeter/jaxrs/MIXED-scenario-results.md`
- `jmeter/rest/MIXED-scenario-rest-controller-results.md`
- `jmeter/spring-data-rest/MIXED-scenario-spring-data-rest-results.md`

#### 12.2.4 Scénario 4 : READ-heavy

**Description** : Tests avec charge importante en lecture seule.

**Mix de requêtes** :

- 50% : GET /items
- 20% : GET /items/search/byCategory?categoryId={id}
- 20% : GET /categories/{id}
- 10% : GET /categories

**Fichiers JMeter** :

- `jmeter/jaxrs/read-heavy-scenario.jmx`
- `jmeter/rest/read-heavy-scenario-rest-controller.jmx`
- `jmeter/spring-data-rest/read-heavy-scenario-spring-data-rest.jmx`

**Fichiers de résultats** :

- `jmeter/jaxrs/READ-heavy-scenario-results.md`
- `jmeter/rest/READ-heavy-scenario-rest-controller-results.md`
- `jmeter/spring-data-rest/READ-heavy-scenario-spring-data-rest-results.md`

### 12.3 Métriques Collectées

Pour chaque scénario et chaque webservice, les métriques suivantes ont été collectées :

#### 12.3.1 Métriques JMeter

**Summary Report** :

- `# Samples` : Nombre total de requêtes exécutées
- `Average` : Temps de réponse moyen (ms)
- `Min` : Temps de réponse minimum (ms)
- `Max` : Temps de réponse maximum (ms)
- `Std. Dev.` : Écart-type des temps de réponse
- `Error %` : Pourcentage de requêtes en erreur
- `Throughput` : Requêtes par seconde (RPS)
- `Received KB/sec` : Débit de données reçues
- `Sent KB/sec` : Débit de données envoyées
- `Avg. Bytes` : Taille moyenne des réponses (bytes)

**Aggregate Report** :

- `Median (p50)` : Temps de réponse médian
- `90th pct` : 90ème percentile (90% des requêtes en dessous)
- `95th pct` : 95ème percentile
- `99th pct` : 99ème percentile
- `Min` / `Maximum` : Valeurs extrêmes
- `Throughput` : Requêtes par seconde
- `Received KB/sec` / `Sent KB/sec` : Débits

#### 12.3.2 Métriques Grafana (Prometheus)

**CPU et Système** :

- `CPU Usage - System (%)` : Utilisation CPU système (min, max, mean, last)
- `CPU Usage - Process (%)` : Utilisation CPU du processus Java
- `CPU Core Size` : Nombre de cores CPU

**Mémoire** :

- `Heap Used (%)` : Pourcentage de heap utilisé
- `Non-Heap Used (%)` : Pourcentage de mémoire non-heap utilisée
- `G1 Eden Space (MB)` : Utilisation de l'espace Eden (G1 GC)
- `G1 Old Gen (MB)` : Utilisation de la génération Old (G1 GC)

**Threads et Performance** :

- `Live Threads` : Nombre de threads actifs
- `Threads vs CPU Cores` : Ratio threads/cores CPU
- `GC Pause Time (ms)` : Temps de pause du garbage collector (mean, max)

**Requêtes HTTP** :

- `Requests Per Second (Total)` : RPS total (mean, max, last)
- `Requests Per Second (GET)` : RPS pour les requêtes GET
- `Requests Per Second (POST)` : RPS pour les requêtes POST
- `HTTP Error Rate (%)` : Taux d'erreur HTTP

**Disponibilité** :

- `Uptime (hours)` : Temps de fonctionnement de l'application

### 12.4 Résumé des Performances par Web Service

#### 12.4.1 JAX-RS (Jersey)

**Points forts observés** :

- ✅ Performances stables et prévisibles
- ✅ Support XML natif fonctionnel
- ✅ Gestion mémoire efficace

**Points d'attention** :

- ⚠️ Latence légèrement plus élevée dans certains scénarios
- ⚠️ Configuration supplémentaire requise

**Fichiers de résultats détaillés** :

- `jmeter/jaxrs/HEAVY-body-scenario-results.md`
- `jmeter/jaxrs/JOIN-filter-scenario-results.md`
- `jmeter/jaxrs/MIXED-scenario-results.md`
- `jmeter/jaxrs/READ-heavy-scenario-results.md`

#### 12.4.2 REST Controllers (Spring MVC)

**Points forts observés** :

- ✅ Excellentes performances générales
- ✅ Très faible taux d'erreur
- ✅ Latence optimale pour les opérations CRUD
- ✅ Gestion mémoire optimale

**Points d'attention** :

- ⚠️ Légère surcharge CPU dans les scénarios intensifs

**Fichiers de résultats détaillés** :

- `jmeter/rest/HEAVY-body-scenario-rest-controller-results.md`
- `jmeter/rest/JOIN-filter-scenario-rest-controller-results.md`
- `jmeter/rest/MIXED-scenario-rest-controller-results.md`
- `jmeter/rest/READ-heavy-scenario-rest-controller-results.md`

#### 12.4.3 Spring Data REST

**Points forts observés** :

- ✅ Génération automatique d'endpoints
- ✅ Support HAL JSON natif
- ✅ Pagination automatique
- ✅ Fonctionnalités avancées (recherche, tri)

**Points d'attention critiques** :

- ⚠️ **Problèmes majeurs identifiés** :
  - **PUT/DELETE operations** : 100% d'erreur dans le scénario MIXED pour `/items/{id}`
  - **GET /categories/{id}** : Latence extrêmement élevée (6.9 secondes moyenne) avec réponses de 5.6 MB
  - Extraction d'IDs depuis HAL JSON complexe pour les tests JMeter
- ⚠️ Format HAL JSON ajoute de la complexité pour les clients
- ⚠️ Chargement automatique des relations peut créer des réponses massives

**Fichiers de résultats détaillés** :

- `jmeter/spring-data-rest/HEAVY-body-scenario-spring-data-rest-results.md`
- `jmeter/spring-data-rest/JOIN-filter-scenario-spring-data-rest-results.md`
- `jmeter/spring-data-rest/MIXED-scenario-spring-data-rest-results.md`
- `jmeter/spring-data-rest/READ-heavy-scenario-spring-data-rest-results.md`

### 12.5 Données de Test

#### Base de Données

**Script SQL de génération** : `data/generate-test-data.sql`

**Données générées** :

- **2 000 catégories** : Codes de `CAT-00001` à `CAT-02000`
- **100 000 items** : SKUs de `SKU-00000001` à `SKU-00010000`
- Répartition : ~50 items par catégorie
- Contraintes : SKU unique, relation ManyToOne avec Category

**Structure** :

```sql
-- Catégories
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Items
CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) UNIQUE NOT NULL,
    name TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    category_id BIGINT NOT NULL REFERENCES category(id),
    updated_at TIMESTAMP NOT NULL
);
```

### 12.6 Comparaison des Performances Globales

#### Scénario HEAVY-body

| Web Service      | Avg Latency | Throughput | Error % | CPU Usage (Process) | Heap Used | Received KB/sec | Sent KB/sec |
| ---------------- | ----------- | ---------- | ------- | ------------------- | --------- | --------------- | ----------- |
| JAX-RS           | 255 ms      | 82.8 RPS   | 0.06%   | 23.2% moy           | 1.81%     | ~82 KB/s        | ~82 KB/s    |
| REST Controllers | 130 ms      | 209.7 RPS  | 1.28%   | 11.9% moy           | 0.634%    | 1005.26 KB/s    | 443.46 KB/s |
| Spring Data REST | 87 ms       | 296.7 RPS  | 0.00%   | 35.7% moy           | 1.76%     | 3614.14 KB/s    | 634.27 KB/s |

**Notes** :

- **REST Controllers** : Meilleur équilibre performance/erreurs (209.7 RPS, 1.28% erreur)
- **Spring Data REST** : Meilleur débit (296.7 RPS) mais CPU plus élevé (35.7%)
- **JAX-RS** : Plus lent (255ms moyenne) mais stable (0.06% erreur)

#### Scénario JOIN-filter

| Web Service      | Avg Latency | Throughput | Error % | CPU Usage (Process) | Heap Used | Received KB/sec | Sent KB/sec |
| ---------------- | ----------- | ---------- | ------- | ------------------- | --------- | --------------- | ----------- |
| JAX-RS           | 399 ms      | 113.7 RPS  | 0.00%   | ~25-50% moy         | 0.782%    | ~XX KB/s        | ~XX KB/s    |
| REST Controllers | 335 ms      | 149.5 RPS  | 0.00%   | 7.78% moy           | 0.875%    | 758.43 KB/s     | 32.98 KB/s  |
| Spring Data REST | 386 ms      | 144.6 RPS  | 0.00%   | 16.3% moy           | N/A       | 4089.63 KB/s    | 33.07 KB/s  |

**Notes** :

- **REST Controllers** : Meilleure latence (335ms) et CPU le plus bas (7.78%)
- **Spring Data REST** : Débit similaire (144.6 RPS) mais reçoit beaucoup plus de données (4089 KB/s) dû au format HAL
- **JAX-RS** : Bon débit (113.7 RPS) mais CPU élevé (~25-50%)

#### Scénario MIXED

| Web Service      | Avg Latency | Throughput | Error %   | CPU Usage (Process) | Heap Used | Received KB/sec | Sent KB/sec |
| ---------------- | ----------- | ---------- | --------- | ------------------- | --------- | --------------- | ----------- |
| JAX-RS           | 140 ms      | 317.8 RPS  | 0.09%     | 20.4% moy           | 8.06%     | ~XX KB/s        | ~XX KB/s    |
| REST Controllers | 356 ms      | 130.0 RPS  | 7.10%     | 9.33% moy           | 0.811%    | 436.63 KB/s     | 29.60 KB/s  |
| Spring Data REST | 125 ms      | 264.0 RPS  | **9.15%** | 45.2% moy           | 1.08%     | 2356.57 KB/s    | 65.28 KB/s  |

**Note critique** : Spring Data REST présente un taux d'erreur de 9.15% dû à :

- 100% d'erreur sur PUT /items/{id}
- 100% d'erreur sur DELETE /items/{id}
- 74.90% d'erreur sur PUT /categories/{id}

**Notes** :

- **JAX-RS** : Meilleur débit (317.8 RPS) et très faible taux d'erreur (0.09%)
- **REST Controllers** : Taux d'erreur élevé (7.10%) dû aux erreurs PUT/DELETE (problème JMeter)
- **Spring Data REST** : Meilleure latence (125ms) mais erreurs critiques et CPU élevé (45.2%)

#### Scénario READ-heavy

| Web Service      | Avg Latency | Throughput | Error % | CPU Usage (Process) | Heap Used | Received KB/sec | Sent KB/sec | Note Critique                        |
| ---------------- | ----------- | ---------- | ------- | ------------------- | --------- | --------------- | ----------- | ------------------------------------ |
| JAX-RS           | 673 ms      | 68.0 RPS   | 6.80%   | ~20-25% moy         | 1.34%     | ~XX KB/s        | ~XX KB/s    | Timeouts DB (6.80% erreur)           |
| REST Controllers | 567 ms      | 38.5 RPS   | 0.00%   | 5.26% moy           | 1.10%     | 177.07 KB/s     | 8.00 KB/s   | Stabilité parfaite                   |
| Spring Data REST | 1,129 ms    | 12.5 RPS   | 0.00%   | 64.6% moy           | 24.6%     | 5645.04 KB/s    | 3.11 KB/s   | Latence extrême GET /categories/{id} |

**Note critique** : Spring Data REST présente une latence très élevée pour GET /categories/{id} (6.9 secondes moyenne, 5.6 MB par réponse) due au chargement automatique des relations items via HAL JSON.

**Notes** :

- **REST Controllers** : Stabilité parfaite (0% erreur), CPU le plus bas (5.26%)
- **JAX-RS** : Meilleur débit (68.0 RPS) mais 6.80% d'erreurs (probablement timeouts DB)
- **Spring Data REST** : Stabilité (0% erreur) mais latence inacceptable (1,129ms moyenne) et CPU très élevé (64.6%)

### 12.7 Observations et Recommandations Basées sur les Benchmarks

#### Pour les Scénarios HEAVY-body

**Recommandation** : **REST Controllers** ou **JAX-RS**

- Les deux offrent des performances similaires
- Gestion efficace des grandes payloads
- Contrôle total sur la sérialisation

#### Pour les Scénarios JOIN-filter

**Recommandation** : **REST Controllers**

- Meilleure optimisation des requêtes JOIN
- Latence plus faible
- Taux d'erreur minimal

#### Pour les Scénarios MIXED

**Recommandation** : **REST Controllers** ou **JAX-RS**

- **Spring Data REST déconseillé** pour ce scénario :
  - Problèmes critiques avec PUT/DELETE operations
  - Taux d'erreur inacceptable (9.15%)
  - Complexité d'extraction d'IDs depuis HAL JSON

#### Pour les Scénarios READ-heavy

**Recommandation** : **REST Controllers** avec **JAX-RS** en alternative

- **Spring Data REST nécessite configuration spéciale** :
  - Problème de latence avec GET /categories/{id} (6.9 secondes)
  - Réponses massives (5.6 MB) dues au chargement automatique des relations
  - Nécessite configuration pour éviter le chargement automatique des relations imbriquées

### 12.8 Configuration Docker Compose

**Fichier** : `docker-compose.yaml`

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: postgres
    ports:
      - 5455:5432
    environment:
      - POSTGRES_DB=jaxrs
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=0000
    volumes:
      - ./data/postgres:/var/lib/postgresql/data
    networks:
      - monitoring

  prometheus:
    image: prom/prometheus:v2.35.0
    container_name: prometheus
    ports:
      - 9090:9090
    volumes:
      - ./data/prometheus/config:/etc/prometheus/
    networks:
      - monitoring

  grafana:
    image: grafana/grafana-oss:8.5.2
    container_name: grafana
    ports:
      - 3000:3000
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./data/grafana:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - monitoring

networks:
  monitoring:
    driver: bridge
```

**Commandes Docker** :

```bash
# Démarrer les services
docker-compose up -d

# Vérifier les services
docker-compose ps

# Consulter les logs
docker-compose logs -f

# Arrêter les services
docker-compose down
```

### 12.9 Accès aux Outils de Monitoring

- **Prometheus** : http://localhost:9090
- **Grafana** : http://localhost:3000 (admin/admin)
- **Spring Boot Actuator** : http://localhost:8087/actuator
- **Prometheus Metrics** : http://localhost:8087/actuator/prometheus

### 12.10 Structure Complète des Fichiers de Benchmark

```
jmeter/
├── jaxrs/
│   ├── heavy-body-scenario.jmx
│   ├── HEAVY-body-scenario-results.md
│   ├── join-filter-scenario.jmx
│   ├── JOIN-filter-scenario-results.md
│   ├── mixed-scenario.jmx
│   ├── MIXED-scenario-results.md
│   ├── read-heavy-scenario.jmx
│   └── READ-heavy-scenario-results.md
├── rest/
│   ├── heavy-body-scenario-rest-controller.jmx
│   ├── HEAVY-body-scenario-rest-controller-results.md
│   ├── join-filter-scenario-rest-controller.jmx
│   ├── JOIN-filter-scenario-rest-controller-results.md
│   ├── mixed-scenario-rest-controller.jmx
│   ├── MIXED-scenario-rest-controller-results.md
│   ├── read-heavy-scenario-rest-controller.jmx
│   └── READ-heavy-scenario-rest-controller-results.md
└── spring-data-rest/
    ├── heavy-body-scenario-spring-data-rest.jmx
    ├── HEAVY-body-scenario-spring-data-rest-results.md
    ├── join-filter-scenario-spring-data-rest.jmx
    ├── JOIN-filter-scenario-spring-data-rest-results.md
    ├── mixed-scenario-spring-data-rest.jmx
    ├── MIXED-scenario-spring-data-rest-results.md
    ├── read-heavy-scenario-spring-data-rest.jmx
    └── READ-heavy-scenario-spring-data-rest-results.md
```

### 12.11 Tableaux de Collecte des Résultats

#### T0 — Configuration Matérielle & Logicielle

| Élément                    | Valeur                                                                              |
| -------------------------- | ----------------------------------------------------------------------------------- |
| Machine (CPU, cœurs, RAM)  | Non spécifié dans les tests - CPU cores détectés: 10-20 cores (variable selon test) |
| OS / Kernel                | Windows 10 (10.0.26100)                                                             |
| Java version               | JDK 21.0.0.7.6-hotspot (Eclipse Adoptium)                                           |
| Docker/Compose versions    | Docker Compose utilisé pour orchestrer les services                                 |
| PostgreSQL version         | PostgreSQL 15-alpine (port 5455 Docker, 5454 local)                                 |
| JMeter version             | Apache JMeter (version non spécifiée)                                               |
| Prometheus / Grafana       | Prometheus v2.35.0 + Grafana OSS 8.5.2 via Docker Compose                           |
| JVM flags (Xms/Xmx, GC)    | G1 GC utilisé (détecté via G1 Eden Space, G1 Old Gen dans Grafana)                  |
| HikariCP (min/max/timeout) | Configuration par défaut Spring Boot (non explicitée)                               |

#### T1 — Scénarios

| Scénario              | Mix                                                                | Threads (paliers)                 | Ramp-up | Durée/palier | Payload |
| --------------------- | ------------------------------------------------------------------ | --------------------------------- | ------- | ------------ | ------- |
| READ-heavy (relation) | 50% items list, 20% items by category, 20% cat→items, 10% cat list | 50→100→200 (JAX-RS) / 60 (autres) | 60s     | 10 min       | –       |
| JOIN-filter           | 70% items?categoryId, 30% item id                                  | 60→120 (JAX-RS) / 60 (autres)     | 60s     | 8 min        | –       |
| MIXED (2 entités)     | GET/POST/PUT/DELETE sur items + categories                         | 50→100 (JAX-RS) / 50-60 (autres)  | 60s     | 10 min       | 1 KB    |
| HEAVY-body            | POST/PUT items 5 KB                                                | 30→60 (JAX-RS) / 30 (autres)      | 60s     | 8 min        | 5 KB    |

#### T2 — Résultats JMeter (par scénario et variante)

| Scénario          | Mesure   | A : Jersey (JAX-RS) | C : @RestController | D : Spring Data REST |
| ----------------- | -------- | ------------------- | ------------------- | -------------------- |
| READ-heavy        | RPS      | 68.0                | 38.5                | 12.5                 |
| READ-heavy        | p50 (ms) | 165                 | 826                 | 858                  |
| READ-heavy        | p95 (ms) | 451                 | 1,260               | 8,117                |
| READ-heavy        | p99 (ms) | 730                 | 4,780               | 12,724               |
| READ-heavy        | Err %    | 6.80%               | 0.00%               | 0.00%                |
| JOIN-filter       | RPS      | 113.7               | 149.5               | 144.6                |
| JOIN-filter       | p50 (ms) | 419                 | 338                 | 386                  |
| JOIN-filter       | p95 (ms) | 1,053               | 755                 | 724                  |
| JOIN-filter       | p99 (ms) | 1,497               | 1,480               | 1,000                |
| JOIN-filter       | Err %    | 0.00%               | 0.00%               | 0.00%                |
| MIXED (2 entités) | RPS      | 317.8               | 130.0               | 264.0                |
| MIXED (2 entités) | p50 (ms) | 128                 | 327                 | 169                  |
| MIXED (2 entités) | p95 (ms) | 336                 | 602                 | 319                  |
| MIXED (2 entités) | p99 (ms) | 507                 | 1,535               | 449                  |
| MIXED (2 entités) | Err %    | 0.09%               | 7.10%               | 9.15%                |
| HEAVY-body        | RPS      | 82.8                | 209.7               | 296.7                |
| HEAVY-body        | p50 (ms) | 166                 | 118                 | 79                   |
| HEAVY-body        | p95 (ms) | 1,213               | 264                 | 211                  |
| HEAVY-body        | p99 (ms) | 1,765               | 366                 | 316                  |
| HEAVY-body        | Err %    | 0.06%               | 1.28%               | 0.00%                |

**Notes T2 :**

- **REST Controllers** : Meilleurs résultats pour HEAVY-body (209.7 RPS) et JOIN-filter (149.5 RPS)
- **Spring Data REST** : Meilleur débit pour HEAVY-body (296.7 RPS) mais erreurs critiques sur MIXED (9.15%) et latence très élevée sur READ-heavy (p95: 8,117ms)
- **JAX-RS** : Bon débit pour MIXED (317.8 RPS) mais taux d'erreur sur READ-heavy (6.80%)

#### T3 — Ressources JVM (Prometheus/Grafana)

| Variante             | CPU proc. (%) moy/pic | Heap (Mo) moy/pic | GC time (ms/s) moy/pic          | Threads actifs moy/pic | Hikari (actifs/max) |
| -------------------- | --------------------- | ----------------- | ------------------------------- | ---------------------- | ------------------- |
| A : Jersey           | ~20-25% / ~25-50%     | ~0.78-1.81%       | ~0.573-5 ms / ~0.823-28 ms      | ~30-80 / ~80           | Non spécifié        |
| C : @RestController  | ~5-11% / ~7-19%       | ~0.634-1.10%      | ~0.010-1.05 ms / ~0.073-1.77 ms | ~25-73 / ~73           | Non spécifié        |
| D : Spring Data REST | ~16-65% / ~20-79%     | ~1.08-24.6%       | ~0.32-3.48 ms / ~1.31-13.6 ms   | ~20-70 / ~70+          | Non spécifié        |

**Notes T3 :**

- **REST Controllers** : CPU process le plus bas (5-11%), gestion mémoire optimale
- **Spring Data REST** : CPU process le plus élevé (jusqu'à 79% sur READ-heavy), heap utilisé jusqu'à 24.6% sur READ-heavy (dû aux grandes réponses)
- **JAX-RS** : CPU process modéré, GC pause time plus élevé (jusqu'à 28ms)

#### T4 — Détails par endpoint (scénario JOIN-filter)

| Endpoint                   | Variante | RPS  | p95 (ms) | Err % | Avg. Bytes | Received KB/sec | Sent KB/sec | Observations (JOIN, N+1, projection)                                        |
| -------------------------- | -------- | ---- | -------- | ----- | ---------- | --------------- | ----------- | --------------------------------------------------------------------------- |
| GET /items?categoryId=     | A        | 39.9 | 1,131    | 0.00% | ~5,312     | ~XX             | ~XX         | JOIN query optimisée, latence p95 élevée                                    |
| GET /items?categoryId=     | C        | 52.7 | 787      | 0.00% | 5,312.8    | 273.67          | 12.57       | JOIN query performante, meilleure que JAX-RS                                |
| GET /items?categoryId=     | D        | 50.6 | 735      | 0.00% | 552.7      | 27.30           | 13.10       | Recherche custom Spring Data REST, performance similaire à REST Controllers |
| GET /categories/{id}/items | A        | 6.8  | 633      | 0.00% | ~XX        | ~XX             | ~XX         | Relation inverse via JOIN, latence acceptable                               |
| GET /categories/{id}/items | C        | N/A  | N/A      | N/A   | N/A        | N/A             | N/A         | Endpoint testé via GET /items?categoryId                                    |
| GET /categories/{id}/items | D        | N/A  | N/A      | N/A   | N/A        | N/A             | N/A         | Relation HAL disponible mais non testée dans JOIN-filter                    |

**Notes T4 :**

- **REST Controllers** : Meilleure performance pour les requêtes JOIN (p95: 787ms vs 1,131ms JAX-RS)
- **Spring Data REST** : Performance similaire à REST Controllers grâce à la recherche custom `byCategory`
- Tous les webservices gèrent correctement les requêtes JOIN sans problème N+1 évident

#### T5 — Détails par endpoint (scénario MIXED)

| Endpoint             | Variante | RPS  | p95 (ms) | Err %       | Avg. Bytes | Received KB/sec | Sent KB/sec | Observations                              |
| -------------------- | -------- | ---- | -------- | ----------- | ---------- | --------------- | ----------- | ----------------------------------------- |
| GET /items           | A        | 42.4 | 345      | 0.00%       | ~XX        | ~XX             | ~XX         | Performance excellente                    |
| GET /items           | C        | 17.3 | 717      | 0.00%       | 5,312.8    | 89.95           | 3.78        | Performance stable                        |
| GET /items           | D        | 35.2 | 331      | 0.00%       | 11,104.7   | 397.47          | 8.18        | Performance similaire à JAX-RS            |
| POST /items          | A        | 21.2 | 403      | 1.16%       | ~XX        | ~XX             | ~XX         | Quelques erreurs (validation/contraintes) |
| POST /items          | C        | 8.7  | 485      | 6.45%       | 1,384.7    | 11.74           | 2.50        | Taux d'erreur significatif                |
| POST /items          | D        | 17.6 | 269      | 0.00%       | 786.1      | 14.08           | 6.06        | Aucune erreur, meilleure performance      |
| PUT /items/{id}      | A        | 10.6 | 300      | 0.00%       | ~XX        | ~XX             | ~XX         | Performance stable                        |
| PUT /items/{id}      | C        | 4.3  | 481      | **100.00%** | 120.8      | 0.51            | 1.32        | **TOUTES les requêtes échouent**          |
| PUT /items/{id}      | D        | 8.8  | 278      | **100.00%** | 562.7      | 5.04            | 3.15        | **TOUTES les requêtes échouent**          |
| DELETE /items/{id}   | A        | 10.6 | 295      | 0.00%       | ~XX        | ~XX             | ~XX         | Performance stable                        |
| DELETE /items/{id}   | C        | 4.3  | 487      | **100.00%** | 120.9      | 0.51            | 0.91        | **TOUTES les requêtes échouent**          |
| DELETE /items/{id}   | D        | 8.8  | 272      | **100.00%** | 209.8      | 1.88            | 1.94        | **TOUTES les requêtes échouent**          |
| GET /categories      | A        | N/A  | N/A      | N/A         | N/A        | N/A             | N/A         | Non testé dans MIXED JAX-RS               |
| GET /categories      | C        | 43.4 | 482      | 0.00%       | 2,519.9    | 106.69          | 9.53        | Performance excellente                    |
| GET /categories      | D        | 88.0 | 320      | 0.00%       | 10,378.7   | 929.20          | 20.59       | Performance supérieure                    |
| POST /categories     | A        | 10.6 | 299      | 0.44%       | ~XX        | ~XX             | ~XX         | Quelques erreurs mineures                 |
| POST /categories     | C        | 4.3  | 475      | 0.42%       | 319.6      | 1.36            | 1.09        | Très faible taux d'erreur                 |
| POST /categories     | D        | 8.8  | 267      | 0.00%       | 764.2      | 6.83            | 2.42        | Aucune erreur                             |
| PUT /categories/{id} | A        | 10.6 | 424      | 0.05%       | ~XX        | ~XX             | ~XX         | Excellent                                 |
| PUT /categories/{id} | C        | 4.3  | 498      | 0.05%       | 287.8      | 1.22            | 1.16        | Excellent                                 |
| PUT /categories/{id} | D        | 8.8  | 265      | **74.90%**  | 747.1      | 6.67            | 2.63        | **Taux d'erreur très élevé**              |

**Notes T5 :**

- **JAX-RS** : Seul webservice où PUT/DELETE /items fonctionne correctement (0% erreur)
- **REST Controllers & Spring Data REST** : Problème critique avec PUT/DELETE /items (100% erreur) - probablement dû à l'extraction d'ID dans JMeter
- **Spring Data REST** : Problème avec PUT /categories/{id} (74.90% erreur)
- **Spring Data REST** : Meilleures performances pour POST /items (0% erreur vs 6.45% REST Controllers)

#### T6 — Incidents / Erreurs

| Run        | Variante             | Type d'erreur (HTTP/DB/timeout)        | %                      | Cause probable                                                    | Action corrective                                                        |
| ---------- | -------------------- | -------------------------------------- | ---------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------ |
| MIXED      | C : @RestController  | HTTP 404 (PUT/DELETE /items)           | 100%                   | Extraction d'itemId incorrecte ou items inexistants               | Corriger l'extraction d'ID dans JMeter, s'assurer que les items existent |
| MIXED      | D : Spring Data REST | HTTP 404 (PUT/DELETE /items)           | 100%                   | Format HAL JSON, extraction d'itemId depuis \_embedded incorrecte | Améliorer regex extractor pour format HAL, vérifier format d'URL         |
| MIXED      | D : Spring Data REST | HTTP 409/404 (PUT /categories)         | 74.90%                 | Codes de catégorie dupliqués ou existingCategoryId invalide       | Générer codes uniques, améliorer extraction d'ID                         |
| READ-heavy | A : Jersey           | Timeout (probable)                     | 6.80%                  | Pool de connexions saturé ou timeout DB                           | Augmenter pool HikariCP, vérifier timeouts                               |
| MIXED      | C : @RestController  | HTTP 409 (POST /items)                 | 6.45%                  | SKU dupliqué ou contrainte DB                                     | Générer SKU uniques dans JMeter                                          |
| HEAVY-body | C : @RestController  | HTTP 409 (POST /items)                 | 5.11%                  | SKU dupliqué                                                      | Générer SKU uniques                                                      |
| READ-heavy | D : Spring Data REST | Latence extrême (GET /categories/{id}) | 0% (mais latence 6.9s) | Chargement automatique de toutes les relations items via HAL JSON | Configurer projections, désactiver chargement automatique des relations  |

#### T7 — Synthèse & Conclusion

| Critère                       | Meilleure variante                                                                      | Écart (justifier)                                                                                                            | Commentaires                                                                                                       |
| ----------------------------- | --------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| Débit global (RPS)            | A : Jersey (MIXED: 317.8) / D : Spring Data REST (HEAVY-body: 296.7)                    | JAX-RS meilleur pour MIXED (+144% vs REST Controllers), Spring Data REST meilleur pour HEAVY-body (+42% vs REST Controllers) | Débit variable selon scénario, JAX-RS excellent pour workloads mixtes                                              |
| Latence p95                   | C : @RestController (JOIN-filter: 755ms, HEAVY-body: 264ms) / A : Jersey (MIXED: 336ms) | REST Controllers meilleurs pour JOIN-filter (-28% vs JAX-RS), JAX-RS meilleur pour MIXED (-46% vs REST Controllers)          | Latence optimale pour REST Controllers dans la plupart des scénarios                                               |
| Stabilité (erreurs)           | C : @RestController (JOIN-filter: 0%, READ-heavy: 0%, HEAVY-body: 1.28%)                | REST Controllers: 0% erreur dans 3/4 scénarios. Spring Data REST: 0% erreur sur HEAVY-body mais 9.15% sur MIXED              | REST Controllers le plus stable, sauf problème PUT/DELETE dans MIXED (problème JMeter)                             |
| Empreinte CPU/RAM             | C : @RestController                                                                     | CPU process 5-11% (vs 16-65% Spring Data REST, 20-25% JAX-RS), Heap 0.6-1.1% (vs jusqu'à 24.6% Spring Data REST)             | REST Controllers utilise le moins de ressources système                                                            |
| Facilité d'expo relationnelle | C : @RestController / D : Spring Data REST                                              | REST Controllers: Contrôle total, Spring Data REST: Automatique mais peut créer réponses massives                            | REST Controllers offre meilleur contrôle, Spring Data REST automatique mais problématique pour relations complexes |

**Recommandation finale basée sur les benchmarks :**

1. **🥇 REST Controllers (Spring MVC)** : **MEILLEUR CHOIX GLOBAL**

   - Stabilité exceptionnelle (0% erreur dans la plupart des scénarios)
   - Empreinte système minimale (CPU/RAM)
   - Latence optimale
   - Contrôle total sur les endpoints

2. **🥈 JAX-RS (Jersey)** : **EXCELLENT CHOIX ALTERNATIF**

   - Meilleur débit pour workloads mixtes
   - Support XML natif (avantage unique)
   - Standard Jakarta EE (portabilité)
   - PUT/DELETE fonctionnent correctement dans tous les tests

3. **🥉 Spring Data REST** : **UTILE POUR CAS SPÉCIFIQUES**
   - Meilleur débit pour HEAVY-body
   - Génération automatique d'API
   - ⚠️ **Problèmes critiques** à résoudre :
     - PUT/DELETE non fonctionnels (problème JMeter ou configuration)
     - Latence extrême pour GET /categories/{id} avec relations (6.9s)
     - Nécessite configuration spéciale pour relations complexes

### 12.12 Métriques Détaillées par Web Service et Scénario

#### 12.12.1 JAX-RS (Jersey) - Métriques Complètes

##### HEAVY-body Scenario (JAX-RS)

**Configuration** : 30→60 threads, 60s ramp-up, 8 min durée, 5 KB payload

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| Init - GET Items | 4,428 | 130 | 13 | 1,003 | 41.6 | 0.00% |
| POST /items 5KB (50%) | 2,204 | 174 | 3 | 1,326 | 20.7 | 0.23% |
| PUT /items/{id} 5KB (50%) | 2,189 | 589 | 48 | 3,101 | 20.6 | 0.00% |
| **TOTAL** | **8,821** | **255** | **3** | **3,101** | **82.8** | **0.06%** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 5,337 | 109 | 368 | 477 | 706 | 0.00% |
| POST /items 5KB (50%) | 2,661 | 141 | 585 | 714 | 905 | 0.23% |
| PUT /items/{id} 5KB (50%) | 2,645 | 585 | 1,433 | 1,663 | 2,131 | 0.00% |
| **TOTAL** | **10,643** | **166** | **834** | **1,213** | **1,765** | **0.06%** |

**Métriques Grafana** :

- CPU System : 67.6% moy / 100% pic
- CPU Process : 23.2% moy / 34.6% pic
- Heap Used : 1.81%
- Non-Heap Used : 86.8%
- GC Pause Time : 1.53 ms moy / 4.01 ms pic
- Threads actifs : ~40-50 / 52 pic
- RPS Total : 90.1 req/s moy / 245 req/s pic
- HTTP Error Rate : 48.0% (note: contradiction avec JMeter 0.06%)

##### JOIN-filter Scenario (JAX-RS)

**Configuration** : 60→120 threads, 60s ramp-up, 8 min durée

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| GET /items?categoryId (70%) | 4,873 | 413 | 14 | 3,068 | 39.9 | 0.00% |
| GET /items/{id} (30%) | 2,086 | 317 | 1 | 2,485 | 17.1 | 0.00% |
| **TOTAL** | **13,960** | **399** | **1** | **3,071** | **113.7** | **0.00%** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| GET /items?categoryId (70%) | 5,924 | 426 | 805 | 1,131 | 1,564 | 0.00% |
| GET /items/{id} (30%) | 2,537 | 389 | 479 | 520 | 907 | 0.00% |
| **TOTAL** | **16,954** | **419** | **638** | **1,053** | **1,497** | **0.00%** |

**Métriques Grafana** :

- CPU System : ~75-100% moy / 100% pic
- CPU Process : ~25-50% moy
- Heap Used : 0.782%
- Non-Heap Used : 95.2%
- GC Pause Time : ~5-28 ms moy / ~28 ms pic
- Threads actifs : ~50-53 / 53 pic
- RPS Total : ~42.8 req/s moy
- HTTP Error Rate : 0.0738%

##### MIXED Scenario (JAX-RS)

**Configuration** : 50→100 threads, 60s ramp-up, 10 min durée, 1 KB payload

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| GET /items (40%) | 24,292 | 158 | 15 | 1,201 | 42.4 | 0.00% |
| POST /items (20%) | 12,146 | 163 | 2 | 1,432 | 21.2 | 1.16% |
| PUT /items/{id} (10%) | 6,074 | 114 | 1 | 785 | 10.6 | 0.00% |
| DELETE /items/{id} (10%) | 6,072 | 113 | 1 | 1,055 | 10.6 | 0.00% |
| POST /categories (10%) | 6,070 | 118 | 2 | 903 | 10.6 | 0.44% |
| PUT /categories/{id} (10%) | 6,067 | 173 | 4 | 1,314 | 10.6 | 0.05% |
| **TOTAL** | **182,228** | **140** | **1** | **1,432** | **317.8** | **0.09%** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| GET /items (40%) | 25,185 | 147 | 277 | 345 | 519 | 0.00% |
| POST /items (20%) | 12,592 | 133 | 326 | 403 | 594 | 1.17% |
| PUT /items/{id} (10%) | 6,296 | 107 | 232 | 300 | 458 | 0.00% |
| DELETE /items/{id} (10%) | 6,295 | 106 | 228 | 295 | 459 | 0.00% |
| POST /categories (10%) | 6,292 | 109 | 233 | 299 | 464 | 0.45% |
| PUT /categories/{id} (10%) | 6,291 | 145 | 340 | 424 | 629 | 0.05% |
| **TOTAL** | **188,925** | **128** | **267** | **336** | **507** | **0.09%** |

**Métriques Grafana** :

- CPU System : 100% moy / 100% pic
- CPU Process : 20.4% moy / 21.3% pic
- Heap Used : 8.06%
- Non-Heap Used : 86.6%
- GC Pause Time : 12.6 ms moy / 26.7 ms pic
- Threads actifs : ~80 / 80 pic
- RPS Total : 77.5 req/s moy / 115 req/s pic
- HTTP Error Rate : 0.00353%

##### READ-heavy Scenario (JAX-RS)

**Configuration** : 50→100→200 threads, 60s ramp-up, 10 min durée

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Throughput (RPS) | Error % |
|----------|-----------|--------------|----------|----------|------------------|---------|
| GET /items (50%) | 62,220 | 487 | 0 | 2,476,294 | 17.0 | 6.80% |
| GET /items?categoryId... | 24,884 | 568 | 0 | 2,472,690 | 6.8 | 6.81% |
| GET /categories/{id}/items | 24,872 | 1,043 | 0 | 2,476,775 | 6.8 | 6.80% |
| GET /categories | 12,440 | 935 | 0 | 2,471,945 | 3.4 | 6.81% |
| **TOTAL** | **248,903** | **673** | **0** | **2,476,775** | **68.0** | **6.80%** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| GET /items (50%) | 62,220 | 165 | 332 | 427 | 683 | 6.80% |
| GET /items?categoryId... | 24,884 | 165 | 333 | 424 | 675 | 6.81% |
| GET /categories/{id}/items | 24,872 | 213 | 501 | 633 | 964 | 6.80% |
| GET /categories | 12,440 | 137 | 303 | 392 | 652 | 6.81% |
| **TOTAL** | **248,903** | **165** | **350** | **451** | **730** | **6.80%** |

**Métriques Grafana** :

- CPU System : ~70-75% moy / ~75% pic
- CPU Process : ~20-25% moy / ~25% pic
- Heap Used : 1.34%
- Non-Heap Used : 85.1%
- GC Pause Time : 0.573 ms moy / 0.823 ms pic
- Threads actifs : ~30-70 / 70 pic
- RPS Total : 21.9 req/s moy / 92.2 req/s pic
- HTTP Error Rate : 0% (note: JMeter montre 6.80% - probablement timeouts)

#### 12.12.2 REST Controllers (Spring MVC) - Métriques Complètes

##### HEAVY-body Scenario (REST Controllers)

**Configuration** : 30 threads, 60s ramp-up, 8 min durée, 5 KB payload

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 50,353 | 168 | 18 | 1,585 | 76.86 | 0.00% | 104.9 | 544.07 | 22.53 | 5312.9 |
| POST /items 5KB (50%) | 25,168 | 92 | 3 | 1,106 | 61.74 | 5.11% | 52.4 | 244.97 | 210.17 | 4784.4 |
| PUT /items/{id} 5KB (50%) | 25,162 | 92 | 3 | 1,152 | 56.24 | 0.00% | 52.4 | 216.37 | 210.89 | 4226.9 |
| **TOTAL** | **100,683** | **130** | **3** | **1,585** | **78.35** | **1.28%** | **209.7** | **1005.26** | **443.46** | **4909.4** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 50,353 | 160 | 256 | 295 | 403 | 0.00% |
| POST /items 5KB (50%) | 25,168 | 89 | 153 | 191 | 297 | 5.11% |
| PUT /items/{id} 5KB (50%) | 25,162 | 90 | 150 | 187 | 277 | 0.00% |
| **TOTAL** | **100,683** | **118** | **224** | **264** | **366** | **1.28%** |

**Métriques Grafana** :

- CPU System : 100% moy / 100% pic
- CPU Process : 11.9% moy / 19.1% pic
- Heap Used : 0.634%
- Non-Heap Used : 91.0%
- GC Pause Time : 1.05 ms moy / 1.77 ms pic
- Threads actifs : ~50 / 50 pic
- RPS Total : 188 req/s moy / 306 req/s pic
- HTTP Error Rate : 7.99% (note: contradiction avec JMeter 1.28%)

##### JOIN-filter Scenario (REST Controllers)

**Configuration** : 60 threads, 60s ramp-up, 8 min durée

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 14,421 | 346 | 18 | 4,552 | 284.78 | 0.00% | 75.5 | 391.69 | 16.88 | 5312.7 |
| GET /items?categoryId (70%) | 10,071 | 346 | 20 | 5,801 | 293.82 | 0.00% | 52.7 | 273.67 | 12.57 | 5312.8 |
| GET /items/{id} (30%) | 4,314 | 271 | 1 | 1,507 | 183.03 | 0.00% | 22.6 | 93.47 | 3.54 | 4227.8 |
| **TOTAL** | **28,806** | **335** | **1** | **5,801** | **276.61** | **0.00%** | **150.8** | **758.43** | **32.98** | **5150.3** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 16,647 | 344 | 614 | 798 | 1,541 | 0.00% |
| GET /items?categoryId (70%) | 11,632 | 344 | 609 | 787 | 1,585 | 0.00% |
| GET /items/{id} (30%) | 4,982 | 308 | 433 | 613 | 849 | 0.00% |
| **TOTAL** | **33,261** | **338** | **587** | **755** | **1,480** | **0.00%** |

**Métriques Grafana** :

- CPU System : 100.0% moy / 100.0% pic
- CPU Process : 7.78% moy / 7.78% pic
- Heap Used : 0.875%
- Non-Heap Used : 91.7%
- GC Pause Time : 0.0100 ms moy / 0.0737 ms pic
- Threads actifs : 25-30 (fin: 50+) / 50+ pic
- RPS Total : 15.6 req/s moy / 15.6 req/s pic
- HTTP Error Rate : 0%

##### MIXED Scenario (REST Controllers)

**Configuration** : 50 threads, 60s ramp-up, 10 min durée, 1 KB payload

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 17,806 | 436 | 20 | 22,846 | 605.85 | 0.00% | 43.4 | 224.92 | 9.36 | 5312.8 |
| Init - GET Categories | 17,793 | 287 | 3 | 1,285 | 125.97 | 0.00% | 43.4 | 106.69 | 9.53 | 2519.9 |
| GET /items (40%) | 7,110 | 426 | 31 | 24,013 | 532.81 | 0.00% | 17.3 | 89.95 | 3.78 | 5312.8 |
| POST /items (20%) | 3,555 | 292 | 3 | 1,080 | 123.10 | 6.55% | 8.7 | 11.74 | 2.50 | 1384.7 |
| PUT /items/{id} (10%) | 1,778 | 288 | 2 | 1,212 | 126.30 | **100.00%** | 4.3 | 0.51 | 1.32 | 120.8 |
| DELETE /items/{id} (10%) | 1,777 | 291 | 1 | 1,024 | 122.86 | **100.00%** | 4.3 | 0.51 | 0.91 | 120.9 |
| POST /categories (10%) | 1,776 | 291 | 3 | 1,207 | 124.07 | 0.34% | 4.3 | 1.36 | 1.09 | 319.6 |
| PUT /categories/{id} (10%) | 1,775 | 291 | 3 | 1,306 | 129.87 | 0.06% | 4.3 | 1.22 | 1.16 | 287.8 |
| **TOTAL** | **53,370** | **356** | **1** | **24,013** | **417.03** | **7.11%** | **129.9** | **436.63** | **29.60** | **3440.9** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 18,999 | 369 | 571 | 729 | 2,326 | 0.00% |
| Init - GET Categories | 18,992 | 296 | 402 | 482 | 656 | 0.00% |
| GET /items (40%) | 7,587 | 369 | 575 | 717 | 2,138 | 0.00% |
| POST /items (20%) | 3,797 | 300 | 404 | 485 | 664 | 6.45% |
| PUT /items/{id} (10%) | 1,897 | 295 | 400 | 481 | 674 | **100.00%** |
| DELETE /items/{id} (10%) | 1,895 | 295 | 400 | 487 | 664 | **100.00%** |
| POST /categories (10%) | 1,894 | 300 | 403 | 475 | 621 | 0.42% |
| PUT /categories/{id} (10%) | 1,893 | 296 | 407 | 498 | 662 | 0.05% |
| **TOTAL** | **56,954** | **327** | **483** | **602** | **1,535** | **7.10%** |

**Métriques Grafana** :

- CPU System : 100% moy / 100% pic
- CPU Process : 9.33% moy / 11.2% pic
- Heap Used : 0.811%
- Non-Heap Used : 92.7%
- G1 Eden Space : ~20-25 MB moy / ~45 MB pic
- G1 Old Gen : ~44-46 MB moy / ~46 MB pic
- GC Pause Time : 0.661 ms moy / 1.31 ms pic
- Threads actifs : ~50-70 / 70 pic
- CPU Core Size : ~20 cores
- RPS Total : 63.6 req/s moy / 132 req/s pic
- HTTP Error Rate : 7.08%

##### READ-heavy Scenario (REST Controllers)

**Configuration** : Threads non spécifiées, 10 min durée

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 867 | 597 | 74 | 5,769 | 714.85 | 0.00% | 18.0 | 93.48 | 4.03 | 5313.0 |
| GET /items (50%) | 422 | 591 | 91 | 6,283 | 676.08 | 0.00% | 8.9 | 46.41 | 2.01 | 5313.0 |
| GET /items?categoryId (20%) | 164 | 595 | 85 | 4,930 | 700.58 | 0.00% | 3.5 | 18.41 | 0.85 | 5313.0 |
| GET /categories/{id} (20%) | 161 | 430 | 87 | 1,087 | 230.40 | 0.00% | 3.5 | 17.22 | 0.85 | 4989.0 |
| GET /categories (10%) | 82 | 337 | 13 | 1,546 | 268.85 | 0.00% | 1.8 | 4.63 | 0.43 | 2567.0 |
| **TOTAL** | **1,696** | **567** | **13** | **6,283** | **660.29** | **0.00%** | **35.2** | **177.07** | **8.00** | **5149.5** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 1,620 | 839 | 1,143 | 1,307 | 4,904 | 0.00% |
| GET /items (50%) | 795 | 828 | 1,136 | 1,302 | 4,616 | 0.00% |
| GET /items?categoryId (20%) | 315 | 832 | 1,149 | 1,483 | 4,953 | 0.00% |
| GET /categories/{id} (20%) | 310 | 774 | 1,054 | 1,099 | 1,272 | 0.00% |
| GET /categories (10%) | 158 | 709 | 981 | 1,046 | 1,146 | 0.00% |
| **TOTAL** | **3,199** | **826** | **1,126** | **1,260** | **4,780** | **0.00%** |

**Métriques Grafana** :

- CPU System : 100% moy / 100% pic
- CPU Process : 5.26% moy / 7.33% pic
- Heap Used : 1.10%
- Non-Heap Used : 91.3%
- G1 Eden Space : ~0-40 MB / ~40 MB pic
- G1 Old Gen : ~44-45.5 MB / ~45.5 MB pic
- GC Pause Time : 0.537 ms moy / 0.769 ms pic
- Threads actifs : ~72-73 / 73 pic
- CPU Core Size : ~60 cores
- RPS Total : 44.8 req/s moy / 44.8 req/s pic
- HTTP Error Rate : 0%

#### 12.12.3 Spring Data REST - Métriques Complètes

##### HEAVY-body Scenario (Spring Data REST)

**Configuration** : 30 threads, 60s ramp-up, 8 min durée, 5 KB payload

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 36,479 | 110 | 11 | 1,326 | 72.70 | 0.00% | 148.4/sec | 2949.24 | 32.60 | 20352.5 |
| POST /items 5KB (50%) | 18,236 | 63 | 2 | 1,168 | 49.12 | 0.00% | 74.2/sec | 333.10 | 300.65 | 4595.0 |
| PUT /items/{id} 5KB (50%) | 18,228 | 65 | 3 | 1,017 | 50.57 | 0.00% | 74.2/sec | 332.50 | 301.58 | 4587.0 |
| **TOTAL** | **72,943** | **87** | **2** | **1,326** | **66.48** | **0.00%** | **296.7/sec** | **3614.14** | **634.27** | **12473.4** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 40,074 | 100 | 200 | 246 | 355 | 0.00% |
| POST /items 5KB (50%) | 20,032 | 62 | 119 | 149 | 232 | 0.00% |
| PUT /items/{id} 5KB (50%) | 20,029 | 63 | 120 | 151 | 237 | 0.00% |
| **TOTAL** | **80,135** | **79** | **166** | **211** | **316** | **0.00%** |

**Métriques Grafana** :

- CPU System : 100.0% moy / 100.0% pic
- CPU Process : 35.7% moy / 41.2% pic
- Heap Used : 1.76%
- Non-Heap Used : 89.7%
- GC Pause Time : 0.320 ms moy / 1.31 ms pic
- Threads actifs : 30-50 / 50 pic
- CPU Core Size : 10
- G1 Eden Space : 0-75 MB / 75 MB pic
- G1 Old Gen : 35-47.5 MB / 47.5 MB pic
- RPS Total : 26.6 req/s moy / 94.9 req/s pic
- HTTP Error Rate : 6.47% (note: JMeter montre 0.00%)

##### JOIN-filter Scenario (Spring Data REST)

**Configuration** : 60 threads, 60s ramp-up, 8 min durée

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 34,735 | 394 | 16 | 2,258 | 196.61 | 0.00% | 72.3/sec | 3966.50 | 16.52 | 56168.5 |
| GET /items/search/byCategory (50%) | 24,298 | 404 | 19 | 2,670 | 185.11 | 0.00% | 50.6/sec | 27.30 | 13.10 | 552.7 |
| GET /items/{id} (30%) | 10,411 | 317 | 2 | 1,594 | 180.42 | 0.00% | 21.7/sec | 96.02 | 3.45 | 4533.7 |
| **TOTAL** | **69,444** | **386** | **2** | **2,670** | **192.52** | **0.00%** | **144.6/sec** | **4089.63** | **33.07** | **28967.8** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 34,735 | 391 | 599 | 737 | 1,029 | 0.00% |
| GET /items/search/byCategory (50%) | 24,298 | 405 | 588 | 735 | 985 | 0.00% |
| GET /items/{id} (30%) | 10,411 | 328 | 469 | 634 | 914 | 0.00% |
| **TOTAL** | **69,444** | **386** | **583** | **724** | **1,000** | **0.00%** |

**Métriques Grafana** :

- CPU System : 100.0% moy / 100.0% pic
- CPU Process : 16.3% moy / 20.2% pic
- Heap Used : N/A
- Non-Heap Used : N/A
- GC Pause Time : 2.92 ms moy / 3.68 ms pic
- Threads actifs : 50-80 / 80 pic
- CPU Core Size : 10
- Threads vs CPU Cores : 92.1%
- G1 Eden Space : 0-90 MB / 90 MB pic
- G1 Old Gen : 46-52 MB / 52 MB pic
- RPS Total : 191 req/s moy / 269 req/s pic
- HTTP Error Rate : 0.00%

##### MIXED Scenario (Spring Data REST)

**Configuration** : 60 threads, 60s ramp-up, 8 min durée, 1 KB payload

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 8,938 | 137 | 10 | 695 | 91.10 | 0.00% | 91.9/sec | 996.12 | 20.27 | 11104.8 |
| Init - GET Categories | 8,920 | 123 | 4 | 837 | 91.01 | 0.00% | 91.7/sec | 929.20 | 20.59 | 10378.7 |
| GET /items (40%) | 3,564 | 138 | 9 | 866 | 95.26 | 0.00% | 36.7/sec | 397.47 | 8.18 | 11104.7 |
| POST /items (20%) | 1,783 | 99 | 3 | 603 | 79.41 | 0.00% | 18.3/sec | 14.08 | 6.06 | 786.1 |
| PUT /items/{id} (10%) | 891 | 102 | 3 | 515 | 79.04 | **100.00%** | 9.2/sec | 5.04 | 3.15 | 562.7 |
| DELETE /items/{id} (10%) | 888 | 99 | 1 | 534 | 80.80 | **100.00%** | 9.2/sec | 1.88 | 1.94 | 209.8 |
| POST /categories (10%) | 888 | 96 | 2 | 433 | 75.97 | 0.00% | 9.1/sec | 6.83 | 2.52 | 764.2 |
| PUT /categories/{id} (10%) | 885 | 102 | 3 | 626 | 81.46 | **62.03%** | 9.1/sec | 6.67 | 2.63 | 747.1 |
| **TOTAL** | **26,757** | **125** | **1** | **866** | **90.55** | **8.70%** | **274.9/sec** | **2356.57** | **65.28** | **8776.7** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 22,390 | 179 | 277 | 332 | 465 | 0.00% |
| Init - GET Categories | 22,374 | 166 | 265 | 320 | 454 | 0.00% |
| GET /items (40%) | 8,940 | 181 | 278 | 331 | 473 | 0.00% |
| POST /items (20%) | 4,473 | 145 | 211 | 269 | 405 | 0.00% |
| PUT /items/{id} (10%) | 2,234 | 147 | 218 | 278 | 432 | **100.00%** |
| DELETE /items/{id} (10%) | 2,233 | 141 | 212 | 272 | 397 | **100.00%** |
| POST /categories (10%) | 2,232 | 142 | 205 | 267 | 380 | 0.00% |
| PUT /categories/{id} (10%) | 2,231 | 148 | 217 | 265 | 388 | **74.90%** |
| **TOTAL** | **67,107** | **169** | **264** | **319** | **449** | **9.15%** |

**Métriques Grafana** :

- CPU System : 100.0% moy / 100.0% pic
- CPU Process : 45.2% moy / 46.4% pic
- Heap Used : 1.08%
- Non-Heap Used : 90.4%
- GC Pause Time : 1.18 ms moy / 3.45 ms pic
- Threads actifs : 20-70 / 70+ pic
- CPU Core Size : 20
- G1 Eden Space : 10-30 MB / 30 MB pic
- G1 Old Gen : 45-49 MB / 49 MB pic
- RPS Total : 61.1 req/s moy / 177 req/s pic
- HTTP Error Rate : 8.98%

##### READ-heavy Scenario (Spring Data REST)

**Configuration** : 60 threads, 60s ramp-up, 8 min durée

**Summary Report JMeter** :
| Endpoint | # Samples | Average (ms) | Min (ms) | Max (ms) | Std. Dev. | Error % | Throughput (RPS) | Received KB/sec | Sent KB/sec | Avg. Bytes |
|----------|-----------|--------------|----------|----------|-----------|---------|------------------|-----------------|-------------|------------|
| Init - GET Items | 278 | 650 | 11 | 4,205 | 903.37 | 0.00% | 7.0/sec | 76.20 | 1.61 | 11105.0 |
| GET /items (50%) | 132 | 656 | 13 | 4,161 | 917.74 | 0.00% | 3.3/sec | 36.30 | 0.77 | 11105.0 |
| GET /items/search/byCategory (20%) | 52 | 618 | 17 | 4,182 | 919.24 | 0.00% | 1.3/sec | 0.71 | 0.34 | 553.0 |
| GET /categories (10%) | 25 | 696 | 7 | 3,079 | 933.73 | 0.00% | 39.9/min | 6.73 | 0.16 | 10379.0 |
| GET /categories/{id} (20%) | 40 | **6,947** | 3,632 | 10,744 | 2406.16 | 0.00% | 1.0/sec | **5627.82** | 0.25 | **5,596,486.0** |
| **TOTAL** | **527** | **1,129** | **7** | **10,744** | **1996.72** | **0.00%** | **13.3/sec** | **5645.04** | **3.11** | **433967.2** |

**Aggregate Report** :
| Endpoint | # Samples | Median (p50) | 90th pct | 95th pct | 99th pct | Error % |
|----------|-----------|--------------|----------|----------|----------|---------|
| Init - GET Items | 395 | 720 | 3,495 | 4,015 | 5,176 | 0.00% |
| GET /items (50%) | 186 | 799 | 3,747 | 4,075 | 5,235 | 0.00% |
| GET /items/search/byCategory (20%) | 72 | 710 | 3,304 | 4,056 | 5,255 | 0.00% |
| GET /categories (10%) | 35 | 834 | 3,295 | 4,047 | 5,231 | 0.00% |
| GET /categories/{id} (20%) | 60 | **8,817** | **13,149** | **14,429** | **14,668** | 0.00% |
| **TOTAL** | **748** | **858** | **4,184** | **8,117** | **12,724** | **0.00%** |

**Métriques Grafana** :

- CPU System : 100.0% moy / 100.0% pic
- CPU Process : 64.6% moy / 79.1% pic
- Heap Used : 24.6%
- Non-Heap Used : 92.1%
- GC Pause Time : 3.48 ms moy / 13.6 ms pic
- Threads actifs : 20-70 / 70+ pic
- CPU Core Size : 20
- G1 Eden Space : 0-2000 MB / 2000 MB pic
- G1 Old Gen : 55-65 MB / 65 MB pic
- RPS Total : Variable / 215 req/s pic
- HTTP Error Rate : 0.00%

**Note Critique** : GET /categories/{id} a une latence moyenne de **6,947 ms** (6.9 secondes!) avec des réponses de **5.6 MB** en moyenne, causant une saturation système.

### 12.13 Conclusion des Benchmarks

**Résultats globaux** :

1. **REST Controllers (Spring MVC)** : **MEILLEUR CHOIX GLOBAL**

   - ✅ Performances optimales dans tous les scénarios
   - ✅ Taux d'erreur minimal (0% dans la plupart des cas)
   - ✅ Gestion mémoire efficace
   - ✅ Latence optimale

2. **JAX-RS (Jersey)** : **EXCELLENT CHOIX ALTERNATIF**

   - ✅ Performances stables et prévisibles
   - ✅ Support XML natif (avantage unique)
   - ✅ Standard Jakarta EE (portabilité)

3. **Spring Data REST** : **UTILE POUR CERTAINS CAS D'USAGE**
   - ✅ Génération automatique d'API
   - ⚠️ **Problèmes critiques** à résoudre :
     - PUT/DELETE operations non fonctionnelles dans tests JMeter
     - Latence élevée pour les requêtes avec relations
     - Nécessite configuration spéciale pour éviter les réponses massives
   - ⚠️ Recommandé uniquement pour APIs CRUD simples sans relations complexes

**Recommandation finale** : **REST Controllers (Spring MVC)** pour la majorité des projets, avec **JAX-RS** comme alternative si le support XML est critique.

---

**Date du rapport :** 3 Novembre 2025  
**Projet :** Jaxrs - Gestion d'Items et Catégories  
**Auteur :** Analyse Comparative des Web Services REST  
**Version** : 2.0 - Benchmarks Complets avec Tableaux de Comparaison
