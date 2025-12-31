<img width="600" height="200" alt="Untitled design" src="https://github.com/user-attachments/assets/fe1c1fd9-606d-4919-89e1-b99d20965e29" />

# 🎭 OCCASIO EVENT — Plateforme SaaS de billetterie

**OCCASIO EVENT** est une application web moderne pour la gestion des réservations d'événements (concerts, conférences, festivals...). Conçue pour le marché marocain et africain, l'application offre une UX "SaaS" fluide avec une interface 100% Java (Vaadin) et une architecture robuste côté serveur (Spring Boot).

Résumé technique
- Backend : Spring Boot 3.x (Java 17+)
- Frontend : Vaadin Flow 24.x (UI server-side en Java)
- Persistance : Spring Data JPA / Hibernate
- Base de données de développement : H2 (mode Auto-Server / fichier)
- Génération PDF : Apache PDFBox
- Build & dépendances : Maven

---

## 🚀 Fonctionnalités principales

Pour les clients
- Page d’accueil immersive avec Hero Section et statistiques.
- Recherche d’événements et filtrage.
- Réservation simplifiée (calculateur de prix en MAD et vérification de disponibilité).
- Génération & téléchargement de tickets officiels au format PDF.
- Dashboard personnel — historique des réservations et dépenses.

Pour les organisateurs
- Création / édition d’événements (Brouillon / Publié).
- Géolocalisation des lieux (lien Google Maps).
- Dashboard business (revenus, taux de remplissage).
- Export CSV des participants.

Administration
- Gestion des utilisateurs, rôles et modération.
- Visualisation et gestion globale des réservations.
- Thème : Dark Mode & Light Mode (préservé entre sessions).

Autres
- UI 100% Java (pas de fichiers CSS obligatoires pour les composants principaux).
- Export / import et endpoints REST pour intégration future.

---

## 🛠 Technologies & bibliothèques

- Java 17+
- Spring Boot 3.x
- Vaadin Flow 24.x
- Spring Security (authentification/autorisation)
- Spring Data JPA + Hibernate
- H2 Database (développement)
- Maven (wrapper `mvnw` inclus)
- Apache PDFBox (génération PDF)
- (Optionnel) Lombok

---

## 📋 Prérequis

- Java JDK 17 ou supérieur
- Maven 3.8+ (ou utilisation du wrapper `./mvnw`)
- Git
- Navigateur moderne (Chrome, Firefox, Edge)

Vérifications :
```bash
java -version
mvn -v
```

---

## ⚙️ Installation & configuration

1. Cloner le dépôt
```bash
git clone https://github.com/soulaimane-22/Event_Reservation.git
cd Event_Reservation
```

2. Configuration de la base de données (H2 — fichier, Auto-Server recommandé)  
> Note : Ton projet contient un fichier d'amorçage `data.sql` situé dans `src/main/resources/data.sql` (voir capture). Ce script initialise la base (comptes, rôles, exemples d'événements). Spring Boot/SQL initializer chargera automatiquement ce fichier si les propriétés suivantes sont activées.

Exemple recommandé à placer / vérifier dans `src/main/resources/application.properties` :

```properties
# Datasource H2 en mode fichier + Auto-Server (permet connexions externes pour debug/intelliJ)
spring.datasource.url=jdbc:h2:file:./data/eventdb;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# IMPORTANT — initialisation SQL
# active l'exécution des scripts SQL (schema.sql / data.sql)
spring.sql.init.mode=always
# s'assurer que l'initialisation SQL s'exécute APRES la création du schéma par Hibernate
spring.jpa.defer-datasource-initialization=true
# (optionnel) préciser la plateforme
spring.sql.init.platform=h2

# Console H2 (DEV uniquement) — désactivez en production
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Pourquoi ces propriétés ?
- `spring.sql.init.mode=always` : force Spring à exécuter `data.sql` (utile en dev avec H2).  
- `spring.jpa.defer-datasource-initialization=true` : différer l'initialisation SQL jusqu'à ce que Hibernate ait créé les tables; évite les erreurs de contraintes lorsque `ddl-auto` crée le schéma.
- `AUTO_SERVER=TRUE` dans l'URL H2 facilite l'ouverture de la base depuis un IDE pendant le développement.

3. Installer les dépendances et compiler
```bash
# Avec le wrapper (préféré)
./mvnw clean install

# ou si Maven est installé
mvn clean install
```

Remarque : la commande `package` déclenche la génération des ressources Vaadin (frontend) si configurée.

---

## 🔁 Initialisation de la base (data.sql)

- Chemin : `src/main/resources/data.sql`  
- Contenu typique : création de rôles, création d'utilisateurs (admin / organizer / client), événements exemples et réservations.
- Quand `spring.sql.init.mode=always` et `spring.jpa.defer-datasource-initialization=true` sont définis, Spring exécute automatiquement `data.sql` au démarrage et les données d'exemple sont insérées après que Hibernate ait préparé le schéma.

Vérifications après démarrage :
- Ouvrez la console H2 (dev) : http://localhost:8089/h2-console (ou le port configuré)
- JDBC URL (exemple) : `jdbc:h2:file:./data/eventdb`
- User : `sa`  — Password : (vide si non défini)
- Requête pour vérifier les utilisateurs : `SELECT * FROM users;` (adapter au nom réel de la table)

---

## 🔑 Comptes de test (initialisation automatique)

Selon `data.sql` fourni, les comptes suivants sont insérés à l'initialisation :

- Administrateur : `admin@event.ma` / `admin123`  
- Organisateur : `organizer1@event.ma` / `org123`  
- Client : `client1@event.ma` / `client123`

(Vérifie `src/main/resources/data.sql` pour la liste exacte et les mots de passe.)

---

## 💻 Lancer l’application

Mode développement (rechargement rapide) :
```bash
# Avec wrapper
./mvnw spring-boot:run

# ou
mvn spring-boot:run
```

Accès :
- UI Vaadin : http://localhost:8089  (ou http://localhost:8089 selon `server.port`)
- Console H2 (dev uniquement) : http://localhost:8089/h2-console

---

## 🔐 Sécurité & bonnes pratiques

- Ne pas laisser la console H2 activée en production.
- Ne pas stocker de secrets (mots de passe, clés) dans le dépôt : utilisez des variables d’environnement ou un vault.
- En production, remplacez H2 par une base persistante (PostgreSQL / MySQL) et adaptez `spring.datasource.url`.
- Activez HTTPS et configurez des politiques CORS / CSP si l’app est exposée publiquement.

---

## 🧭 Structure du projet (extrait)
- src/main/java/.../entity — Entités JPA (User, Event, Reservation, Ticket, etc.)
- src/main/java/.../repository — Repositories Spring Data
- src/main/java/.../service — Services métier (réservation, txt, export CSV)
- src/main/java/.../security — Configuration Spring Security
- src/main/java/.../view — Vues Vaadin (Public, Client, Organizer, Admin)
- src/main/resources/static — Images , svg, et assets statiques
- src/main/resources/application.properties — Configuration
- src/main/resources/data.sql — Données d’amorçage (comptes, exemple d’événement)

---

## 🧪 Tests

Exécuter les tests unitaires et d’intégration :
```bash
./mvnw test
```

Si vous avez des tests d’intégration avec une base H2 dédiée, vérifiez que les profiles et propriétés sont correctement définis.

---

## 📦 Export  CSV

- L’export des participants est disponible en CSV depuis l’interface organisateur (ou via une route d’API dédiée si implémentée).

---

## 📈 Roadmap & améliorations prévues

- Intégration d’un fournisseur de paiement (Stripe, CMI).
- Envoi automatique des tickets par email (Spring Mail ou un service externe).
- Authentification multi-tenants pour support SaaS multi-organisateurs.
- Ratings
- Déploiement Docker / Docker Compose + configuration facile pour une instance cloud.
- Analytics temps réel / notifications push.

---

## ✉️ Contact & contribution

- Développeur & Mainteneur : **soulaimane-22**
- Email: benayad.soulaimane@etu.uae.ac.ma
- Projet : Travail de Java Avancé — Spring Boot & Vaadin
