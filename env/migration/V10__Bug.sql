CREATE TABLE IF NOT EXISTS bug (
  id       SERIAL PRIMARY KEY,
  created_at TIMESTAMP(3) NOT NULL DEFAULT now(),
  fixed_at TIMESTAMP(3),
  theme    TEXT NOT NULL,
  bug_case TEXT NOT NULL,
  fixer_id INTEGER REFERENCES "user" (id),
  status   INTEGER DEFAULT 0,
  info     TEXT
);