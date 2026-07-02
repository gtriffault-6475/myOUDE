---
baseline_commit: 424c4b737268229ecd76aa7a02da91be7e6a96b9
---

# Story 1.1 — Bootstrap projet : Angular + Spring Boot + PostgreSQL + GKE

**Epic :** E1 — Accès sécurisé à l'application (SSO + fondations)
**JIRA :** SCRUM-11
**Statut :** in-progress
**Date :** 2026-06-30

---

## User Story

En tant que membre de l'équipe technique,
Je veux que l'infrastructure de base soit bootstrappée et déployable sur GKE,
Afin que l'équipe puisse développer et déployer les fonctionnalités OUDE en itérations rapides.

---

## Critères d'acceptance

**Étant donné** que les deux pods (Front + Back) sont déployés sur GKE
**Quand** une requête HTTP est envoyée depuis le front Angular vers le back Spring Boot
**Alors** le back répond avec un endpoint `/actuator/health` retournant `{ "status": "UP" }`
**Et** la communication passe par un Service Kubernetes interne (ClusterIP), pas via Apigee

**Étant donné** que le pod Back est démarré
**Quand** Spring Boot initialise
**Alors** la connexion à Cloud SQL PostgreSQL est établie via Workload Identity (sans secret en variable d'environnement ou dans le code)
**Et** Flyway (ou Liquibase) s'exécute et applique la migration initiale (table `schema_version` ou équivalent)

**Étant donné** qu'un commit est mergé sur la branche principale
**Quand** la pipeline CI/CD s'exécute
**Alors** les images Docker Front et Back sont buildées et poussées dans Artifact Registry
**Et** les manifests Kubernetes sont appliqués sur le cluster GKE

**Étant donné** que le back Spring Boot génère des logs
**Quand** un appel HTTP est reçu
**Alors** les logs sont en JSON structuré (format compatible GCP Cloud Logging)
**Et** le niveau INFO est utilisé par défaut, ERROR sur exception

---

## Scope de la Story 1.1

### IN SCOPE
- Scaffolding Angular workspace (app shell vide, routing module, HttpClient configuré)
- Scaffolding Spring Boot (module web, actuator, datasource, Flyway/Liquibase)
- Configuration Cloud SQL via Workload Identity (pas de mot de passe en dur)
- Manifests GKE : 2 Deployments (front + back), 2 Services (ClusterIP), 1 Namespace `oude`
- Pipeline CI/CD minimale : build → push Artifact Registry → kubectl apply
- Logs structurés JSON dans Spring Boot
- Healthcheck endpoint `/actuator/health` accessible depuis le front

### OUT OF SCOPE (stories suivantes)
- SSO / authentification (Story 1.2)
- Navigation et routing Angular (Story 1.3)
- Connexion à Apigee X
- Toute logique métier

---

## Stack technique — versions cibles

| Composant | Technologie | Version cible | Notes |
|-----------|-------------|---------------|-------|
| Frontend | Angular | 18.x LTS | Pattern S1 Renault |
| Frontend build | Node.js | 20.x LTS | Node LTS pour Angular 18 |
| Backend | Java | 21 LTS | Pattern S2/S7 Renault |
| Backend | Spring Boot | 3.3.x | Spring Security 6.x inclus |
| Backend build | Maven | 3.9.x | ou Gradle 8.x selon préférence équipe |
| Base de données | PostgreSQL | 15.x | Cloud SQL — Pattern S3 Renault |
| Migrations | Flyway | 10.x | intégré Spring Boot 3.3 |
| Conteneurs | Docker | 24.x+ | multi-stage build |
| Orchestration | Kubernetes | 1.29+ (GKE) | Pattern VPC DCE/Podgroup |
| Registry | Artifact Registry | GCP | remplace Container Registry |
| Logs backend | Logback + logstash-logback-encoder | 7.4+ | JSON structuré GCP |

---

## Structure des fichiers à créer

```
/oude-frontend/                          ← workspace Angular
  angular.json
  package.json                           ← deps: @angular/core@18, rxjs, zone.js
  src/
    app/
      app.component.ts                   ← shell minimaliste
      app.routes.ts                      ← routing module vide
    environments/
      environment.ts                     ← { apiUrl: '/api' }
      environment.prod.ts                ← { apiUrl: '/api' }
  Dockerfile                             ← multi-stage: node build + nginx serve

/oude-backend/                           ← projet Spring Boot
  pom.xml                                ← deps: spring-boot-starter-web, actuator,
                                            datasource, flyway, logback-encoder
  src/main/java/com/renault/oude/
    OudeApplication.java                 ← @SpringBootApplication
    config/
      LoggingConfig.java                 ← configuration JSON structuré
    health/
      HealthController.java              ← (optionnel — /actuator/health suffit)
  src/main/resources/
    application.yml                      ← voir section configuration ci-dessous
    db/migration/
      V1__init.sql                       ← table schema_test (validation connectivité)
  Dockerfile                             ← multi-stage: maven build + JRE 21

/k8s/                                    ← manifests Kubernetes
  namespace.yaml                         ← namespace: oude
  backend/
    deployment.yaml                      ← pod back Spring Boot
    service.yaml                         ← ClusterIP port 8080
  frontend/
    deployment.yaml                      ← pod front nginx
    service.yaml                         ← ClusterIP port 80 (ou LoadBalancer pour accès externe dev)

/.github/workflows/                      ← CI/CD (si GitHub Actions)
  ci.yml                                 ← build + push + deploy
```

---

## Configuration Spring Boot (`application.yml`)

```yaml
spring:
  application:
    name: oude-backend
  datasource:
    url: jdbc:postgresql://${DB_HOST}/${DB_NAME}
    # PAS de username/password en clair : Workload Identity gère l'auth Cloud SQL
    # Utiliser le Cloud SQL Auth Proxy (sidecar GKE) ou socket Unix
  flyway:
    enabled: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

logging:
  config: classpath:logback-spring.xml
```

---

## Configuration Workload Identity + Cloud SQL

### Approche recommandée : Cloud SQL Auth Proxy en sidecar

```yaml
# Dans k8s/backend/deployment.yaml
spec:
  template:
    spec:
      serviceAccountName: oude-backend-sa   # KSA lié au GSA via Workload Identity
      containers:
        - name: oude-backend
          image: REGION-docker.pkg.dev/PROJECT/oude/backend:TAG
          env:
            - name: DB_HOST
              value: "127.0.0.1"             # auth proxy écoute localhost
            - name: DB_NAME
              value: "oude_db"
          ports:
            - containerPort: 8080
        - name: cloud-sql-proxy             # sidecar
          image: gcr.io/cloud-sql-connectors/cloud-sql-proxy:2.x
          args:
            - "--private-ip"
            - "PROJECT:REGION:INSTANCE_NAME"
```

### Setup Workload Identity (commandes une fois)

```bash
# 1. Créer le Google Service Account
gcloud iam service-accounts create oude-backend-sa \
  --project=PROJECT_ID

# 2. Donner accès Cloud SQL Client
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member="serviceAccount:oude-backend-sa@PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/cloudsql.client"

# 3. Lier GSA ↔ KSA via Workload Identity
gcloud iam service-accounts add-iam-policy-binding \
  oude-backend-sa@PROJECT_ID.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:PROJECT_ID.svc.id.goog[oude/oude-backend-sa]"

# 4. Annoter le Kubernetes Service Account
kubectl annotate serviceaccount oude-backend-sa \
  --namespace oude \
  iam.gke.io/gcp-service-account=oude-backend-sa@PROJECT_ID.iam.gserviceaccount.com
```

**⚠️ IMPORTANT :** Aucun `username`/`password` PostgreSQL dans application.yml ni dans les variables d'environnement. L'auth Cloud SQL est entièrement gérée par IAM + Workload Identity.

---

## Logs structurés JSON — configuration Logback

```xml
<!-- src/main/resources/logback-spring.xml -->
<configuration>
  <appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <includeMdcKeyName>traceId</includeMdcKeyName>
      <includeMdcKeyName>spanId</includeMdcKeyName>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="JSON_STDOUT"/>
  </root>
</configuration>
```

Dépendance Maven à ajouter :
```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
</dependency>
```

---

## Dockerfile Front (Angular + Nginx)

```dockerfile
# Stage 1 : build
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Stage 2 : serve
FROM nginx:alpine
COPY --from=builder /app/dist/oude-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

```nginx
# nginx.conf — proxy /api → backend service Kubernetes
server {
  listen 80;
  location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;
  }
  location /api/ {
    proxy_pass http://oude-backend-svc:8080/;
  }
}
```

**⚠️ Le nom `oude-backend-svc` doit correspondre exactement au nom du Service Kubernetes backend.**

---

## Dockerfile Back (Spring Boot)

```dockerfile
# Stage 1 : build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2 : run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Migration initiale Flyway

```sql
-- V1__init.sql — table de validation connectivité uniquement
CREATE TABLE IF NOT EXISTS schema_version_check (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE schema_version_check IS 'Table de validation bootstrap — à supprimer post-Lot 1';
```

---

## Communication Front ↔ Back (AD-3)

- Le front Angular appelle **uniquement** `/api/*` via le proxy Nginx → Service ClusterIP backend
- **Jamais** via Apigee (AD-3 : Apigee n'est pas sur le chemin front→back)
- Dans `environment.ts` : `apiUrl: '/api'` (relatif, résolu par Nginx)
- `HttpClient` Angular configuré avec `provideHttpClient()` dans `app.config.ts`

---

## Vérifications de complétion

- [ ] `curl http://FRONTEND_URL/api/actuator/health` retourne `{ "status": "UP" }`
- [ ] Les logs Spring Boot sont en JSON structuré (vérifier dans GCP Cloud Logging)
- [ ] La migration Flyway V1 est appliquée (vérifier table `schema_version_check` dans Cloud SQL)
- [ ] Les images sont dans Artifact Registry (pas Docker Hub)
- [ ] Aucun secret (mot de passe, service account key) en variable d'environnement ou dans le code
- [ ] Les deux pods sont `Running` dans le namespace `oude` : `kubectl get pods -n oude`
- [ ] Le Service Kubernetes backend est de type ClusterIP (non exposé à l'extérieur directement)

---

## Guardrails anti-erreurs

| ❌ Anti-pattern | ✅ À faire |
|-----------------|-----------|
| Stocker des credentials Cloud SQL dans application.yml | Utiliser Cloud SQL Auth Proxy + Workload Identity |
| Exposer le backend via un LoadBalancer | Service ClusterIP uniquement — le front proxy les appels |
| Appeler le backend Angular via URL hardcodée | Utiliser `/api` relatif + proxy Nginx |
| Utiliser `ng serve` en production | Nginx multi-stage Dockerfile |
| Utiliser `console.log()` côté back | Logback JSON structuré via logstash-logback-encoder |
| Logger des données client en INFO | Données sensibles en DEBUG seulement |
| Installer Cloud SQL connector Java à la place du proxy | Le sidecar proxy est l'approche GKE standard Renault |
| Mettre les variables GCP (PROJECT_ID, INSTANCE_NAME) dans le code | Variables d'environnement Kubernetes ConfigMap/Secret |

---

## Questions ouvertes (ne pas bloquer la story)

- **AQ-PROJECT** : Quel est le `PROJECT_ID` GCP et la `REGION` cible ? (À obtenir auprès de l'équipe ops Renault)
- **AQ-CLUSTER** : Nom du cluster GKE et namespace autorisé ? (Vérifier le Pattern VPC DCE/Podgroup Renault)
- **AQ-REGISTRY** : URL Artifact Registry configurée ? Format : `REGION-docker.pkg.dev/PROJECT/oude`
- **AQ-INSTANCE** : Nom de l'instance Cloud SQL (à créer ou existant ?)

Si ces informations ne sont pas disponibles, bootstrapper avec un `docker-compose.yml` local (PostgreSQL local) pour permettre le développement en attendant les réponses Renault. La structure du code doit rester identique.

---

## Notes de développement

**Environnement local (en l'absence des réponses GCP) :**
```yaml
# docker-compose.yml pour dev local
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: oude_db
      POSTGRES_USER: oude_dev
      POSTGRES_PASSWORD: dev_only_not_prod
    ports:
      - "5432:5432"
  backend:
    build: ./oude-backend
    environment:
      DB_HOST: db
      DB_NAME: oude_db
      SPRING_DATASOURCE_USERNAME: oude_dev
      SPRING_DATASOURCE_PASSWORD: dev_only_not_prod
    ports:
      - "8080:8080"
  frontend:
    build: ./oude-frontend
    ports:
      - "4200:80"
```

**⚠️ Ce docker-compose est UNIQUEMENT pour le dev local. En GKE, Workload Identity remplace entièrement les variables username/password.**
