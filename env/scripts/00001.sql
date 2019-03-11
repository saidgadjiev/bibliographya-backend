CREATE TABLE IF NOT EXISTS "user" (
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP(3) DEFAULT NOW(),
  password VARCHAR(1024) NOT NULL,
  email VARCHAR(512) NOT NULL,
  email_verified BOOLEAN DEFAULT FALSE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE (email, email_verified)
);

INSERT INTO "user"("email", "email_verified", "password") VALUES ('g.said.alievich@mail.ru', TRUE, '$2a$10$V.hNtSdrn5Jmhxd1wMDZ6eo.q2EY0gO/v4pm7HqRoPx7vc8NBZyIO');

