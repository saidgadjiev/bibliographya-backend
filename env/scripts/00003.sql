CREATE TABLE IF NOT EXISTS user_role (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES "user"(id),
  role_name VARCHAR (255) NOT NULL REFERENCES role(name),
  UNIQUE (user_id, role_name)
);

INSERT INTO user_role("user_id", "role_name") VALUES(1, 'ROLE_ADMIN');
INSERT INTO user_role("user_id", "role_name") VALUES(2, 'ROLE_USER');