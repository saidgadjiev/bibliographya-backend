CREATE TABLE IF NOT EXISTS biography_report (
  id SERIAL PRIMARY KEY,
  reported_at TIMESTAMP DEFAULT NOW(),
  reporter_id INTEGER NOT NULL REFERENCES "user"(id),
  consider_id INTEGER REFERENCES "user"(id),
  biography_id INTEGER NOT NULL REFERENCES biography(id),
  status INTEGER NOT NULL DEFAULT 0,
  reason INTEGER NOT NULL,
  reason_text TEXT
);