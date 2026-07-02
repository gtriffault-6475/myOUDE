-- Leads du portefeuille vendeur
CREATE TABLE leads (
    id                       VARCHAR(50)  PRIMARY KEY,
    portefeuille_id          VARCHAR(50)  NOT NULL,
    nom_client               VARCHAR(255),
    prenom_client            VARCHAR(255),
    email_client             VARCHAR(255),
    telephone_client         VARCHAR(50),
    score_potentiel          INTEGER,
    statut                   VARCHAR(50),
    modele_interesse         VARCHAR(255),
    date_creation            TIMESTAMP,
    derniere_interaction     TIMESTAMP,
    derniere_synchronisation TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Affaires en cours du portefeuille vendeur
CREATE TABLE affaires (
    id                       VARCHAR(50)  PRIMARY KEY,
    portefeuille_id          VARCHAR(50)  NOT NULL,
    nom_client               VARCHAR(255),
    prenom_client            VARCHAR(255),
    modele                   VARCHAR(255),
    type_financement         VARCHAR(50),
    statut                   VARCHAR(50),
    date_creation            TIMESTAMP,
    echeance                 TIMESTAMP,
    derniere_synchronisation TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Métadonnées de synchronisation par portefeuille
CREATE TABLE portefeuille_sync (
    portefeuille_id          VARCHAR(50)  PRIMARY KEY,
    derniere_synchronisation TIMESTAMP    NOT NULL DEFAULT NOW(),
    source                   VARCHAR(20)  NOT NULL DEFAULT 'api'
);

CREATE INDEX idx_leads_portefeuille   ON leads(portefeuille_id);
CREATE INDEX idx_affaires_portefeuille ON affaires(portefeuille_id);
