# Comment tester Prometheus

## ✅ Test de connexion de base

### 1. Interface Web Prometheus
Ouvrez dans votre navigateur : **http://localhost:9090**

Vous devriez voir l'interface web de Prometheus.

### 2. Vérifier les targets (sources de métriques)
Allez sur : **http://localhost:9090/targets**

Vous devriez voir le job `spring-boot-jaxrs` avec un statut **UP** (en vert).

### 3. Tester une requête simple
Dans Prometheus, allez dans l'onglet **Graph** et essayez :
```
up
```
Cela devrait retourner toutes les métriques disponibles.

### 4. Test de l'API avec paramètres

#### Test via navigateur (requête simple) :
```
http://localhost:9090/api/v1/query?query=up
```

#### Test via curl (PowerShell) :
```powershell
curl "http://localhost:9090/api/v1/query?query=up"
```

#### Test de métriques Spring Boot :
```
http://localhost:9090/api/v1/query?query=http_server_requests_seconds_count
```

## 🔧 Configuration Grafana

Dans Grafana, utilisez simplement :
- **URL :** `http://prometheus:9090`
- **Access :** Server (default)

Grafana teste automatiquement la connexion en utilisant l'endpoint `/api/v1/status/config` qui ne nécessite pas de paramètres.

## ❌ Erreur courante

Si vous accédez à `http://localhost:9090/api/v1/query` **sans paramètres**, vous obtiendrez :
```json
{
  "status": "error",
  "errorType": "bad_data",
  "error": "invalid parameter \"query\": 1:1: parse error: no expression found in input"
}
```

**C'est normal !** Il faut toujours fournir un paramètre `query`.

## 📊 Métriques Spring Boot disponibles

Une fois que votre application Spring Boot démarre, vous pouvez interroger :

```
# Nombre total de requêtes HTTP
http_server_requests_seconds_count

# Temps de réponse moyen
http_server_requests_seconds_sum / http_server_requests_seconds_count

# Requêtes par méthode HTTP
http_server_requests_seconds_count{method="GET"}

# JVM mémoire
jvm_memory_used_bytes

# Threads
jvm_threads_live_threads
```


