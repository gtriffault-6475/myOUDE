-- Migration multi-pays : colonnes en anglais (langue technique neutre) + country_code
-- AD-11 : noms techniques EN, valeurs de statut codes EN, country_code sur entités partagées

-- ── Table leads ─────────────────────────────────────────────────────────────
ALTER TABLE leads RENAME COLUMN portefeuille_id       TO portfolio_id;
ALTER TABLE leads RENAME COLUMN nom_client            TO client_last_name;
ALTER TABLE leads RENAME COLUMN prenom_client         TO client_first_name;
ALTER TABLE leads RENAME COLUMN email_client          TO client_email;
ALTER TABLE leads RENAME COLUMN telephone_client      TO client_phone;
ALTER TABLE leads RENAME COLUMN score_potentiel       TO potential_score;
ALTER TABLE leads RENAME COLUMN statut                TO status;
ALTER TABLE leads RENAME COLUMN modele_interesse      TO vehicle_model;
ALTER TABLE leads RENAME COLUMN date_creation         TO created_at;
ALTER TABLE leads RENAME COLUMN derniere_interaction  TO last_interaction_at;
ALTER TABLE leads RENAME COLUMN derniere_synchronisation TO last_sync_at;

ALTER TABLE leads ADD COLUMN country_code VARCHAR(2) NOT NULL DEFAULT 'fr';

DROP INDEX IF EXISTS idx_leads_portefeuille;
CREATE INDEX idx_leads_portfolio ON leads(portfolio_id);
CREATE INDEX idx_leads_country   ON leads(country_code);

-- ── Table affaires ───────────────────────────────────────────────────────────
ALTER TABLE affaires RENAME COLUMN portefeuille_id     TO portfolio_id;
ALTER TABLE affaires RENAME COLUMN nom_client          TO client_last_name;
ALTER TABLE affaires RENAME COLUMN prenom_client       TO client_first_name;
ALTER TABLE affaires RENAME COLUMN modele              TO vehicle_model;
ALTER TABLE affaires RENAME COLUMN type_financement    TO financing_type;
ALTER TABLE affaires RENAME COLUMN statut              TO status;
ALTER TABLE affaires RENAME COLUMN date_creation       TO created_at;
ALTER TABLE affaires RENAME COLUMN echeance            TO due_at;
ALTER TABLE affaires RENAME COLUMN derniere_synchronisation TO last_sync_at;

ALTER TABLE affaires ADD COLUMN country_code VARCHAR(2) NOT NULL DEFAULT 'fr';

DROP INDEX IF EXISTS idx_affaires_portefeuille;
CREATE INDEX idx_deals_portfolio ON affaires(portfolio_id);
CREATE INDEX idx_deals_country   ON affaires(country_code);

-- ── Table portefeuille_sync → portfolio_sync ──────────────────────────────────
ALTER TABLE portefeuille_sync RENAME TO portfolio_sync;
ALTER TABLE portfolio_sync RENAME COLUMN portefeuille_id         TO portfolio_id;
ALTER TABLE portfolio_sync RENAME COLUMN derniere_synchronisation TO last_sync_at;
