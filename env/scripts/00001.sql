CREATE TABLE IF NOT EXISTS "user" (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT NOW(),
  provider_id VARCHAR(30) NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "user_account" (
  id SERIAL PRIMARY KEY,
  email VARCHAR(512) NOT NULL,
  email_verified BOOLEAN DEFAULT FALSE,
  password VARCHAR(1024) NOT NULL,
  user_id INTEGER NOT NULL REFERENCES "user"(id),
  UNIQUE (email, email_verified)
);

CREATE TABLE IF NOT EXISTS "social_account" (
  id SERIAL PRIMARY KEY,
  account_id VARCHAR(30) UNIQUE NOT NULL,
  user_id INTEGER NOT NULL REFERENCES "user"(id)
);

INSERT INTO "user"(provider_id) VALUES ('email_password');

INSERT INTO "user_account"("email", "email_verified", "password", user_id) VALUES('g.said.alievich@mail.ru', TRUE, '$2a$10$V.hNtSdrn5Jmhxd1wMDZ6eo.q2EY0gO/v4pm7HqRoPx7vc8NBZyIO', 1);

