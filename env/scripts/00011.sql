CREATE TABLE IF NOT EXISTS email_verification (
  id SERIAL PRIMARY KEY,
  email VARCHAR(512) UNIQUE NOT NULL,
  code INTEGER NOT NULL,
  expired_at TIMESTAMP NOT NULL
)