CREATE TABLE IF NOT EXISTS biography_fix (
  id           SERIAL PRIMARY KEY,
  created_at   TIMESTAMP(3)        DEFAULT now(),
  fix_text     TEXT    NOT NULL,
  biography_id INTEGER REFERENCES biography (id),
  creator_id   INTEGER NOT NULL REFERENCES "user" (id),
  fixer_id     INTEGER REFERENCES "user" (id),
  info         TEXT,
  status       INTEGER NOT NULL DEFAULT 0
)