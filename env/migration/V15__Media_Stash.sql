CREATE TABLE IF NOT EXISTS media_stash (
  id SERIAL PRIMARY KEY,
  path VARCHAR(512) NOT NULL UNIQUE,
  created_at TIMESTAMP(3) NOT NULL DEFAULT now()
)