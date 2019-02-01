CREATE TABLE IF NOT EXISTS biography_fix (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT now(),
  fix_text TEXT NOT NULL,
  biography_id INTEGER REFERENCES biography(id) ON DELETE CASCADE,
  creator_id INTEGER NOT NULL REFERENCES "user"(id),
  fixer_id INTEGER REFERENCES "user"(id),
  info TEXT,
  status INTEGER NOT NULL DEFAULT 0
)