CREATE TABLE IF NOT EXISTS biography_fix (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT now(),
  fix_text TEXT NOT NULL,
  biography_id INTEGER REFERENCES biography(id),
  creator_id INTEGER NOT NULL REFERENCES "user"(id),
  fixer_id INTEGER REFERENCES "user"(id),
  status INTEGER NOT NULL DEFAULT 0
)