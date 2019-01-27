CREATE TABLE IF NOT EXISTS "user" (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT NOW(),
  provider_id VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS "user_account" (
  id SERIAL PRIMARY KEY,
  name VARCHAR(128) UNIQUE NOT NULL,
  password VARCHAR(1024) NOT NULL,
  user_id INTEGER NOT NULL REFERENCES "user"(id)
);

CREATE TABLE IF NOT EXISTS "social_account" (
  id SERIAL PRIMARY KEY,
  account_id VARCHAR(30) UNIQUE NOT NULL,
  user_id INTEGER NOT NULL REFERENCES "user"(id)
);

INSERT INTO "user"(provider_id) VALUES ('username_password');

INSERT INTO "user_account"("name", "password", user_id) VALUES('admin', '$2a$10$V.hNtSdrn5Jmhxd1wMDZ6eo.q2EY0gO/v4pm7HqRoPx7vc8NBZyIO', 1);

