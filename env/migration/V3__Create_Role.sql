CREATE TABLE IF NOT EXISTS role (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO role("name") VALUES('ROLE_ADMIN');

INSERT INTO role("name") VALUES('ROLE_MODERATOR');

INSERT INTO role("name") VALUES('ROLE_USER');

INSERT INTO role("name") VALUES('ROLE_DEVELOPER');

CREATE TABLE IF NOT EXISTS user_role (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES "user"(id),
  role_name VARCHAR (255) NOT NULL REFERENCES role(name),
  UNIQUE (user_id, role_name)
);

INSERT INTO user_role("user_id", "role_name") VALUES(1, 'ROLE_ADMIN');
INSERT INTO user_role("user_id", "role_name") VALUES(1, 'ROLE_MODERATOR');
INSERT INTO user_role("user_id", "role_name") VALUES(1, 'ROLE_DEVELOPER');
INSERT INTO user_role("user_id", "role_name") VALUES(1, 'ROLE_USER');