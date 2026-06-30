# OUDE — One Unified Dealer Experience

Outil de travail quotidien des vendeurs du réseau Renault. OUDE centralise le portefeuille client, les scores de potentiel, les recommandations IA et les actions commerciales dans une interface unique.

**POC Lot 1 — deadline : 27 juillet 2026**

---

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Frontend | Angular 18 + Angular Material 18 |
| Backend | Java 21 / Spring Boot 3.3 |
| Base de données | PostgreSQL 15 (Cloud SQL) |
| Infrastructure | GKE (Google Kubernetes Engine) |
| Auth | SSO Renault (JWT) + Spring Security OAuth2 Resource Server |
| API externes | Apigee X |
| IA | Vertex AI Gemini |
| Logs | Logstash-logback-encoder → GCP Cloud Logging |
| Migrations DB | Flyway 10.x |

---

## Structure du projet

```
myOUDE/
├── oude-backend/          # Spring Boot — API REST BFF
│   ├── src/
│   │   ├── main/java/com/renault/oude/
│   │   └── main/resources/
│   ├── Dockerfile
│   └── pom.xml
├── oude-frontend/         # Angular 18 — SPA
│   ├── src/
│   ├── nginx.conf
│   └── Dockerfile
├── k8s/                   # Manifests Kubernetes
│   ├── namespace.yaml
│   ├── backend/
│   └── frontend/
└── docker-compose.yml     # Dev local (sans GCP)
```

---

## Démarrage en local

### Prérequis

- Docker Desktop
- Java 21
- Node.js 20+
- Maven 3.9+

### Lancer l'environnement complet

```bash
docker-compose up -d
```

Démarre PostgreSQL 15, le backend Spring Boot et le frontend Angular.

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Actuator health | http://localhost:8080/actuator/health |

### Backend seul (développement)

```bash
cd oude-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend seul (développement)

```bash
cd oude-frontend
npm install
ng serve
```

---

## Authentification

En local, le profil `local` active un **mock SSO** — aucune connexion à l'IdP Renault réel n'est requise. L'identité mock est configurable dans `oude-backend/src/main/resources/application-local.yml`.

En GKE, le JWT est émis par le SSO Renault et validé via JWKS URI (voir `application-gke.yml`).

> **AQ-1 ouverte** : le format exact du token SSO Renault (claims, JWKS URI, issuer) est en attente de confirmation de l'équipe sécurité Renault.

---

## Déploiement GKE

```bash
# Build et push des images
docker build -t REGION-docker.pkg.dev/PROJECT/oude/backend:TAG ./oude-backend
docker build -t REGION-docker.pkg.dev/PROJECT/oude/frontend:TAG ./oude-frontend
docker push ...

# Appliquer les manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/backend/
kubectl apply -f k8s/frontend/
```

> Les valeurs `PROJECT`, `REGION` et le nom du cluster sont en attente (AQ-5 à AQ-7).

L'accès à Cloud SQL se fait via **Cloud SQL Auth Proxy** en sidecar (Workload Identity) — aucun secret en variable d'environnement.

---

## Sécurité — règles impératives

- Pas de secrets dans le code ou les variables d'environnement
- Pas de fichiers `.env`, `credentials.json` ou `service-account*.json` commités
- Authentification via **Workload Identity** GKE uniquement
- Secrets stockés dans **GCP Secret Manager**

---

## Questions ouvertes

Les questions bloquantes pour Renault sont centralisées dans :
`../planning-artifacts/open-questions-renault.md`

Les questions critiques semaine 1 : **AQ-1** (SSO), **AQ-5 à AQ-8** (infrastructure GCP).

---

## Liens

- JIRA : [SCRUM @ gtriffault.atlassian.net](https://gtriffault.atlassian.net/jira/software/projects/SCRUM/boards)
- GitHub : [gtriffault-6475/myOUDE](https://github.com/gtriffault-6475/myOUDE)
