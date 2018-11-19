CREATE TABLE IF NOT EXISTS biography_comment (
  id SERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  biography_id INTEGER NOT NULL REFERENCES biography(id),
  user_name VARCHAR(128) NOT NULL REFERENCES "user"(name),
  parent_id INTEGER REFERENCES biography_comment(id)
);