CREATE TABLE IF NOT EXISTS "user" (
  id SERIAL PRIMARY KEY,
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
INSERT INTO "user"(provider_id) VALUES ('username_password');

INSERT INTO "user_account"("name", "password", user_id) VALUES('admin', '$2a$10$MItPOf2gYp7D5MOTw.Jl7O8.NTOtxvpQiR65apQ04QRonrMjQdTKe', 1);
INSERT INTO "user_account"("name", "password", "user_id") VALUES('test', '$2a$10$mXyMQeOzYCyBWdCwi42NGeVegfdl2H7uBIXRSFMkG02t5FEeGo8X6', 2);

