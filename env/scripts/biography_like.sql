CREATE TABLE IF NOT EXISTS biography_like (
  id SERIAL PRIMARY KEY,
  biography_id INTEGER NOT NULL REFERENCES biography(id),
  user_name VARCHAR(128) NOT NULL REFERENCES "user"(name),
  UNIQUE (biography_id, user_name)
)