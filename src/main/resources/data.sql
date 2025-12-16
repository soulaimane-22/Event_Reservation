-- ============================================
-- 📄 data.sql - UTILISATEURS ET ÉVÉNEMENTS
-- 📁 Chemin: src/main/resources/data.sql
-- ============================================

-- ========== UTILISATEURS ==========

-- 1. ADMIN (admin@event.ma / admin123)
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone, theme_preference)
VALUES ('Admin', 'System', 'admin@event.ma', '$2a$10$N9qo8uLOickgx2Z6.HvGCeYRXvHhBWrXw/5VBhqXyZ5Xn0vhHpYPS', 'ADMIN', CURRENT_TIMESTAMP, true, '0600000001', 'LIGHT');

-- 2. ORGANIZER 1 (organizer1@event.ma / org123)
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone, theme_preference)
VALUES ('Alami', 'Youssef', 'organizer1@event.ma', '$2a$10$N9qo8uLOickgx2Z6.HvGCeYRXvHhBWrXw/5VBhqXyZ5Xn0vhHpYPS', 'ORGANIZER', CURRENT_TIMESTAMP, true, '0600000002', 'LIGHT');

-- 3. ORGANIZER 2 (organizer2@event.ma / org123)
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone, theme_preference)
VALUES ('Bennani', 'Fatima', 'organizer2@event.ma', '$2a$10$N9qo8uLOickgx2Z6.HvGCeYRXvHhBWrXw/5VBhqXyZ5Xn0vhHpYPS', 'ORGANIZER', CURRENT_TIMESTAMP, true, '0600000003', 'LIGHT');

-- 4. CLIENT 1 (client1@event.ma / client123)
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone, theme_preference)
VALUES ('El Fassi', 'Ahmed', 'client1@event.ma', '$2a$10$N9qo8uLOickgx2Z6.HvGCeYRXvHhBWrXw/5VBhqXyZ5Xn0vhHpYPS', 'CLIENT', CURRENT_TIMESTAMP, true, '0600000004', 'LIGHT');

-- 5. CLIENT 2 (client2@event.ma / client123)
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone, theme_preference)
VALUES ('Idrissi', 'Nadia', 'client2@event.ma', '$2a$10$N9qo8uLOickgx2Z6.HvGCeYRXvHhBWrXw/5VBhqXyZ5Xn0vhHpYPS', 'CLIENT', CURRENT_TIMESTAMP, true, '0600000005', 'LIGHT');


-- ========== ÉVÉNEMENTS ==========

-- Organisateur 1: Youssef Alami (ID sera 2) - 5 événements

-- 1. Summer Music Festival (Organisateur 1)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🎵 Summer Music Festival',
        'Rejoignez-nous pour le plus grand festival de musique de l''année mettant en vedette des artistes internationaux et des talents locaux. Trois jours de musique non-stop, de nourriture et de divertissement.',
        'CONCERT',
        DATEADD('DAY', 30, CURRENT_TIMESTAMP),
        DATEADD('DAY', 33, CURRENT_TIMESTAMP),
        'Complexe Sportif Mohammed V',
        'Casablanca',
        33.5731,
        -7.5898,
        5000,
        4850,
        250.00,
        '/images/events/summer-music-festival.jpg',
        2,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 2. Classic Drama: Hamlet (Organisateur 1)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🎭 Classic Drama: Hamlet',
        'Une performance à couper le souffle du chef-d''œuvre de Shakespeare par la National Theatre Company. Vivez la tragédie du prince danois.',
        'THEATRE',
        DATEADD('DAY', 20, CURRENT_TIMESTAMP),
        DATEADD('DAY', 20, CURRENT_TIMESTAMP),
        'Théâtre Mohammed V',
        'Rabat',
        34.0144,
        -6.8326,
        600,
        520,
        150.00,
        '/images/events/hamlet-theatre.jpg',
        2,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 3. Jazz Night Under Stars (Organisateur 1)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🎷 Jazz Night Under Stars',
        'Une soirée intime de performances jazz douces sous le ciel étoilé. Mettant en vedette des musiciens de jazz renommés et des artistes émergents.',
        'CONCERT',
        DATEADD('DAY', 15, CURRENT_TIMESTAMP),
        DATEADD('DAY', 15, CURRENT_TIMESTAMP),
        'Jardin Majorelle',
        'Marrakech',
        31.6295,
        -7.9811,
        300,
        280,
        200.00,
        '/images/events/jazz-night.jpg',
        2,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 4. Marathon International de Casablanca (Organisateur 1)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🏃 Marathon International de Casablanca',
        'Le plus grand marathon d''Afrique du Nord avec des participants du monde entier. Parcourez les rues emblématiques de Casablanca dans une course qui allie sport et découverte culturelle.',
        'SPORT',
        DATEADD('DAY', 90, CURRENT_TIMESTAMP),
        DATEADD('DAY', 90, CURRENT_TIMESTAMP),
        'Boulevard de la Corniche',
        'Casablanca',
        33.5731,
        -7.5898,
        10000,
        8500,
        150.00,
        '/images/events/marathon-casa.jpg',
        2,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 5. Literature Festival: Words & Stories (Organisateur 1)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('📚 Literature Festival: Words & Stories',
        'Célébrez la littérature avec des auteurs renommés, des ateliers d''écriture et des lectures publiques. Un rendez-vous incontournable pour les amoureux des mots et des histoires.',
        'CONFERENCE',
        DATEADD('DAY', 50, CURRENT_TIMESTAMP),
        DATEADD('DAY', 53, CURRENT_TIMESTAMP),
        'Bibliothèque Nationale du Royaume',
        'Rabat',
        34.0077,
        -6.8452,
        500,
        450,
        100.00,
        '/images/events/literature-festival.jpg',
        2,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);


-- Organisateur 2: Fatima Bennani (ID sera 3) - 5 événements

-- 6. Tech Innovation Summit 2025 (Organisateur 2)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🚀 Tech Innovation Summit 2025',
        'Explorez l''avenir de la technologie avec des leaders de l''industrie, des startups et des innovateurs. Conférences plénières, ateliers et opportunités de réseautage.',
        'CONFERENCE',
        DATEADD('DAY', 45, CURRENT_TIMESTAMP),
        DATEADD('DAY', 47, CURRENT_TIMESTAMP),
        'Sofitel Rabat Jardin des Roses',
        'Rabat',
        34.0209,
        -6.8416,
        800,
        750,
        500.00,
        '/images/events/tech-summit.jpg',
        3,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 7. Football Championship Finals (Organisateur 2)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('⚽ Football Championship Finals',
        'L''ultime affrontement footballistique entre les deux meilleures équipes de la saison. Vivez l''excitation et la passion du football marocain.',
        'SPORT',
        DATEADD('DAY', 60, CURRENT_TIMESTAMP),
        DATEADD('DAY', 60, CURRENT_TIMESTAMP),
        'Stade Mohammed V',
        'Casablanca',
        33.5657,
        -7.6291,
        45000,
        38000,
        100.00,
        '/images/events/football-finals.jpg',
        3,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 8. Art Exhibition: Contemporary Masters (Organisateur 2)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🎨 Art Exhibition: Contemporary Masters',
        'Une exposition exceptionnelle mettant en vedette les œuvres de maîtres de l''art contemporain. Découvrez des peintures, sculptures et installations innovantes qui repoussent les limites de la créativité.',
        'AUTRE',
        DATEADD('DAY', 10, CURRENT_TIMESTAMP),
        DATEADD('DAY', 40, CURRENT_TIMESTAMP),
        'Musée Mohammed VI d''Art Moderne',
        'Rabat',
        34.0201,
        -6.8352,
        200,
        180,
        80.00,
        '/images/events/art-exhibition.jpg',
        3,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 9. Stand-up Comedy Night (Organisateur 2)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🎤 Stand-up Comedy Night',
        'Une soirée hilarante avec les meilleurs humoristes marocains et internationaux. Rires garantis dans une ambiance chaleureuse et conviviale au cœur de Marrakech.',
        'AUTRE',
        DATEADD('DAY', 25, CURRENT_TIMESTAMP),
        DATEADD('DAY', 25, CURRENT_TIMESTAMP),
        'Théâtre Royal de Marrakech',
        'Marrakech',
        31.6340,
        -7.9897,
        400,
        350,
        120.00,
        '/images/events/comedy-night.jpg',
        3,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);

-- 10. Circus Spectacular: Magic & Wonder (Organisateur 2)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, latitude, longitude, capacite_max, capacite_restante, prix_unitaire, image_url, organisateur_id, statut, date_creation, moyenne_notes, nombre_avis)
VALUES ('🎪 Circus Spectacular: Magic & Wonder',
        'Le plus grand spectacle de cirque jamais organisé au Maroc. Acrobates, jongleurs, clowns et magiciens vous transportent dans un monde de merveilles et d''émerveillement.',
        'AUTRE',
        DATEADD('DAY', 35, CURRENT_TIMESTAMP),
        DATEADD('DAY', 38, CURRENT_TIMESTAMP),
        'Complexe Al Amal',
        'Casablanca',
        33.5882,
        -7.6114,
        2000,
        1800,
        180.00,
        '/images/events/circus-spectacular.jpg',
        3,
        'PUBLIE',
        CURRENT_TIMESTAMP,
        0,
        0);


-- ========== INFORMATIONS DE CONNEXION ==========
--
-- ADMIN:
--   Email: admin@event.ma
--   Password: admin123
--
-- ORGANIZER 1 (Youssef Alami - 5 événements):
--   Email: organizer1@event.ma
--   Password: org123
--
-- ORGANIZER 2 (Fatima Bennani - 5 événements):
--   Email: organizer2@event.ma
--   Password: org123
--
-- CLIENT 1 (Ahmed El Fassi):
--   Email: client1@event.ma
--   Password: client123
--
-- CLIENT 2 (Nadia Idrissi):
--   Email: client2@event.ma
--   Password: client123
--
-- ========== RÉPARTITION DES ÉVÉNEMENTS ==========
--
-- ORGANISATEUR 1 (Youssef Alami):
--   1. 🎵 Summer Music Festival (Casablanca)
--   2. 🎭 Classic Drama: Hamlet (Rabat)
--   3. 🎷 Jazz Night Under Stars (Marrakech)
--   4. 🏃 Marathon International de Casablanca (Casablanca)
--   5. 📚 Literature Festival (Rabat)
--
-- ORGANISATEUR 2 (Fatima Bennani):
--   6. 🚀 Tech Innovation Summit 2025 (Rabat)
--   7. ⚽ Football Championship Finals (Casablanca)
--   8. 🎨 Art Exhibition (Rabat)
--   9. 🎤 Stand-up Comedy Night (Marrakech)
--   10. 🎪 Circus Spectacular (Casablanca)