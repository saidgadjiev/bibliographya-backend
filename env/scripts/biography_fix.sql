CREATE TABLE IF NOT EXISTS biography_fix (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT now(),
  fix_text TEXT NOT NULL,
  biography_id INTEGER REFERENCES biography(id),
  creator_name VARCHAR(128) NOT NULL REFERENCES "user"(name),
  fixer_name VARCHAR(128) REFERENCES "user"(name),
  status INTEGER NOT NULL DEFAULT 0
)