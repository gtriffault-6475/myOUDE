-- Migration initiale OUDE Lot 1
-- Validation de la connectivité PostgreSQL

CREATE TABLE IF NOT EXISTS schema_version_check (
  id      SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE schema_version_check IS 'Table de validation bootstrap — à supprimer post-Lot 1';
