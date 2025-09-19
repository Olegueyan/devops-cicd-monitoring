# Documentation du projet User CRUD API

## 1. Présentation du projet

Cette application est une **API REST CRUD pour gérer des utilisateurs**, construite avec **Kotlin + Ktor**, et conteneurisée avec **Docker**.
Elle utilise :

* **MariaDB** pour la base de données
* **Flyway** pour la gestion des migrations SQL
* **Nginx** comme reverse proxy
* **SLF4J** pour le logging applicatif
* **Ktor ContentNegotiation** avec JSON pour les échanges

**Endpoints principaux :**

| Méthode | Route             | Description                      |
| ------- | ----------------- | -------------------------------- |
| GET     | /api/users        | Liste tous les utilisateurs      |
| GET     | /api/users/{uuid} | Récupère un utilisateur par UUID |
| POST    | /api/users        | Crée un nouvel utilisateur       |
| PUT     | /api/users/{uuid} | Met à jour un utilisateur        |
| DELETE  | /api/users/{uuid} | Supprime un utilisateur          |
| GET     | /health           | Vérifie la santé de l’API + DB   |

---

## 2. Variables d’environnement

Fichier `.env` à la racine du projet :

```dotenv
# -------------------------
# Base de données MariaDB
# -------------------------
DB_HOST=localhost        # Adresse de la base de données (docker-compose service ou IP)
DB_PORT=3306             # Port sur lequel MariaDB écoute
DB_NAME=crud_db          # Nom de la base de données
DB_USER=root             # Utilisateur MariaDB
DB_PASSWORD=password     # Mot de passe MariaDB
DB_JDBC_URL="jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}"  # URL JDBC utilisée par Ktor

# -------------------------
# Application Ktor
# -------------------------
APP_PORT=8080             # Port sur lequel l'application Ktor écoute
```

**Explications détaillées :**

* `DB_HOST` : le nom du service ou l'adresse IP de la base MariaDB. Dans Docker, c'est souvent le nom du service `db`.
* `DB_PORT` : le port MariaDB exposé pour la connexion (par défaut 3306).
* `DB_NAME` : le nom de la base de données utilisée par l'application.
* `DB_USER` : utilisateur ayant les droits sur `DB_NAME`.
* `DB_PASSWORD` : mot de passe correspondant à `DB_USER`.
* `DB_JDBC_URL` : URL complète pour que Ktor se connecte à MariaDB.
* `APP_PORT` : port sur lequel le serveur Ktor sera exposé dans le container (accessible via Nginx).

---

## 3. Gestion des migrations Flyway

### 3.1. Structure des migrations

* Les fichiers SQL sont placés dans `db/migrations/`
* Exemple de migration :

```sql
-- db/migrations/V1__create_users_table.sql
CREATE TABLE users (
    uuid CHAR(36) PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    study_level VARCHAR(255) NOT NULL,
    age INT NOT NULL
);
```

* Chaque fichier doit être nommé avec le format **`V{numéro}__{description}.sql`**
* Flyway exécute les migrations dans l’ordre croissant (`V1`, `V2`, …)

### 3.2. Lancer une migration

* Script Windows fourni : `apply-migration.bat`

```bat
docker-compose run --rm flyway
```

* Cela lance le service Flyway qui applique toutes les migrations manquantes sur la base MariaDB.
* Les migrations utilisent les variables `.env` pour l’utilisateur et le mot de passe.

---

## 4. Build de l’application

Compiler le projet Kotlin avec Gradle :

```bash
./gradlew build
```

* Le JAR final sera généré dans `app/build/libs/app.jar`
* Ce JAR est monté dans le container Docker via `docker-compose.yml`

---

## 5. Lancer l’application

```bash
docker-compose up --build
```

* Services lancés :

| Service    | Port exposé | Description                    |
| ---------- | ----------- | ------------------------------ |
| db         | 3306        | MariaDB                        |
| phpmyadmin | 8081        | Interface web pour gérer la DB |
| flyway     | -           | Applique les migrations SQL    |
| app        | 80          | Application Ktor + Nginx       |

* Accès à l’API : [http://localhost/api/users](http://localhost/api/users)
* Monitoring : [http://localhost/health](http://localhost/health)
* PhpMyAdmin : [http://localhost:8081](http://localhost:8081)

---

## 6. Fonctionnement général

1. **Containerisation** :

    * L’application Ktor et Nginx sont dans le même container (`app`)
    * Nginx fait du reverse proxy vers Ktor
    * Les logs sont stockés dans `/var/logs/crud/`

2. **Migrations** :

    * Flyway exécute automatiquement les SQL dans `db/migrations/` sur MariaDB
    * Chaque migration est versionnée pour assurer la cohérence de la DB

3. **Application Ktor** :

    * Routes CRUD (`/api/users`)
    * Validation des entrées et gestion des erreurs (`400`, `404`, `500`)
    * Logs SLF4J pour chaque action

4. **Observabilité** :

    * Logs applicatifs dans `/var/logs/crud/app.log`
    * Logs Nginx accès : `/var/logs/crud/access.log`
    * Logs Nginx erreur : `/var/logs/crud/error.log`

---

## 7. Résumé des commandes

| Action                   | Commande                    |
| ------------------------ | --------------------------- |
| Build du projet          | `./gradlew build`           |
| Appliquer les migrations | `apply-migration.bat`       |
| Lancer l’application     | `docker-compose up --build` |
| Arrêter les services     | `docker-compose down`       |

---

✅ Avec cette configuration, **il suffit de** :

```bash
./gradlew build
apply-migration.bat
docker-compose up --build
```

et l’API, la base de données et PhpMyAdmin seront opérationnels.
