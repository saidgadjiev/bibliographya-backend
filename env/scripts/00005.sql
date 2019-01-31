CREATE TABLE IF NOT EXISTS biography_like (
  id SERIAL PRIMARY KEY,
  biography_id INTEGER NOT NULL REFERENCES user(id),
  user_id INTEGER NOT NULL REFERENCES "user"(id),
  UNIQUE (biography_id, user_id)
)