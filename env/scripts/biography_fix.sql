CREATE TABLE IF NOT EXISTS biography_fix (
  id SERIAL PRIMARY KEY,
  fix_text TEXT NOT NULL,
  biography_id INTEGER REFERENCES biography(id),
  creator_name VARCHAR(128) REFERENCES "user"(name),
  fixer_name VARCHAR(128) REFERENCES "user"(name),
  status INTEGER NOT NULL DEFAULT 0
)