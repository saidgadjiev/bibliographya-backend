CREATE TABLE IF NOT EXISTS user_role (
  id SERIAL PRIMARY KEY,
  user_name VARCHAR (255) NOT NULL REFERENCES "user"(name),
  role_name VARCHAR (255) NOT NULL REFERENCES role(name),
  UNIQUE (user_name, role_name)
);

INSERT INTO user_role("user_name", "role_name") VALUES('admin', 'ROLE_ADMIN');
INSERT INTO user_role("user_name", "role_name") VALUES('test', 'ROLE_USER');