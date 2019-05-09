CREATE TABLE IF NOT EXISTS user_account (
  id       SERIAL PRIMARY KEY,
  password VARCHAR(1024)   NOT NULL,
  email    VARCHAR(512),
  phone    VARCHAR(512) UNIQUE,
  user_id  INTEGER UNIQUE  NOT NULL REFERENCES "user" (id)
);

INSERT INTO user_account (password, email, phone, user_id) SELECT
                                                             "password",
                                                             email,
                                                             phone,
                                                             id
                                                           FROM "user";

ALTER TABLE "user"
  DROP COLUMN IF EXISTS "password";
ALTER TABLE "user"
  DROP COLUMN IF EXISTS email;
ALTER TABLE "user"
  DROP COLUMN IF EXISTS phone;
ALTER TABLE "user"
  ADD COLUMN IF NOT EXISTS provider_id VARCHAR(30);

UPDATE "user" SET provider_id = 'simple';

ALTER TABLE "user" ALTER COLUMN provider_id SET NOT NULL;

CREATE TABLE IF NOT EXISTS social_account (
  id         SERIAL PRIMARY KEY,
  account_id VARCHAR(30) UNIQUE   NOT NULL,
  user_id    INTEGER UNIQUE       NOT NULL REFERENCES "user" (id)
);