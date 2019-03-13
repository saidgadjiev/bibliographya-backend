CREATE TABLE IF NOT EXISTS biography_comment (
  id SERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT NOW(),
  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,
  user_id INTEGER NOT NULL REFERENCES "user"(id),
  parent_id INTEGER REFERENCES biography_comment(id)
);