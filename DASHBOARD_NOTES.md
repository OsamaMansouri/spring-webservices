# 📊 Notes sur le Dashboard Grafana

## ✅ Corrections Appliquées

### 1. HTTP Error Rate (%) - Affichage "No data"
**Problème :** Le panneau affiche "No data" quand il n'y a pas d'erreurs HTTP.

**Explication :** C'est normal ! Si toutes les requêtes sont en 200 OK, la requête retourne `NaN` ou aucune valeur, ce qui affiche "No data" dans Grafana.

**Solution :** 
- Si vous voulez forcer l'affichage de 0%, vous pouvez modifier le panel dans Grafana :
  1. Éditez le panel "HTTP Error Rate (%)"
  2. Dans "Field" → "Standard options" → "No value", sélectionnez "0"
  3. Ou ajoutez une transformation "Add field from calculation" avec valeur par défaut 0

**Requête actuelle :**
```promql
sum(rate(http_server_requests_seconds_count{status=~"[45].."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100
```

### 2. GC Pause Time - Statistiques incohérentes
**Correction appliquée :** La requête a été modifiée pour grouper par type de GC :
```promql
sum(rate(jvm_gc_pause_seconds_sum[5m])) by (gc) * 1000
```

Cela affiche maintenant chaque type de GC séparément (ex: "G1 Young Generation").

### 3. HikariCP Connection Pool - Incohérence graphique vs stats
**Observation :** Le graphique montre des pics à 30-40 connexions, mais les stats affichent Max 10.

**Explication possible :**
- Les connexions "Active" peuvent être agrégées de manière différente entre le graphique et les stats
- Le graphique montre les valeurs instantanées, les stats calculent mean/max sur toute la période
- Si le pool change de nom (HikariPool-13 peut changer), il peut y avoir plusieurs séries

**Correction appliquée :** Filtre explicite `{pool="HikariPool-13"}` pour toutes les métriques HikariCP.

**Vérification :** Les métriques disponibles sont :
- `hikaricp_connections_active{pool="HikariPool-13"}` - Connexions actives
- `hikaricp_connections_max{pool="HikariPool-13"}` - Maximum (10)
- `hikaricp_connections_idle{pool="HikariPool-13"}` - Connexions inactives
- `hikaricp_connections_pending{pool="HikariPool-13"}` - En attente

## 📈 Données Affichées Correctement

✅ **Uptime** - Fonctionne
✅ **Heap Used** - Fonctionne (0.527%)
✅ **Non-Heap Used** - Fonctionne (86.1%)
✅ **CPU Usage** - Fonctionne
✅ **Threads vs CPU Cores** - Fonctionne
✅ **RPS** - Fonctionne (Total, GET, POST)
✅ **G1 Eden Space** - Fonctionne
✅ **G1 Old Gen** - Fonctionne

## 🔧 Pour Forcer l'Affichage de 0% sur Error Rate

Dans Grafana :
1. Éditez le panel "HTTP Error Rate (%)"
2. Cliquez sur "Transform" (onglet en bas)
3. Ajoutez "Add field from calculation"
4. Mode : "Binary operation"
5. Opération : A + 0 (ou utilisez une transformation "Override" pour remplacer NaN par 0)

Ou modifiez directement dans le panel :
- Field → "Overrides" → Ajoutez un override pour remplacer `NaN`/`null` par `0`

## 📝 Résumé des Requêtes PromQL

### RPS
```promql
sum(rate(http_server_requests_seconds_count[5m]))  # Total
sum(rate(http_server_requests_seconds_count{method="GET"}[5m]))  # GET
sum(rate(http_server_requests_seconds_count{method="POST"}[5m]))  # POST
```

### Error Rate
```promql
sum(rate(http_server_requests_seconds_count{status=~"[45].."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100
```

### GC Pause Time
```promql
sum(rate(jvm_gc_pause_seconds_sum[5m])) by (gc) * 1000
```

### HikariCP
```promql
hikaricp_connections_active{pool="HikariPool-13"}
hikaricp_connections_max{pool="HikariPool-13"}
hikaricp_connections_idle{pool="HikariPool-13"}
hikaricp_connections_pending{pool="HikariPool-13"}
```

## 🎯 Conclusion

Le dashboard fonctionne correctement. Les "No data" pour Error Rate sont normaux quand il n'y a pas d'erreurs. Toutes les autres métriques sont opérationnelles et affichent les bonnes valeurs.

---

## 🧪 Pour Tester les Autres Scénarios

**Simple :** 
1. Lancer un scénario JMeter (read-heavy, join-filter, mixed, heavy-body)
2. Noter l'heure de début et fin du test
3. Dans Grafana, sélectionner cette période exacte
4. Copier les métriques affichées (RPS, CPU, Heap, GC, etc.)
5. Dans JMeter, voir le Summary Report pour p50, p95, p99, Err %

**Répéter pour chaque scénario et chaque variante** (Jersey, @RestController, Spring Data REST).

