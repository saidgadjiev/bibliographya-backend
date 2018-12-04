CREATE TABLE IF NOT EXISTS biography_moderation (
  id SERIAL PRIMARY KEY,
  status VARCHAR(128) NOT NULL DEFAULT 'На модерации',
  moderator_name VARCHAR(128) REFERENCES "user"(name),
  moderated_at TIMESTAMP,
  biography_id INTEGER REFERENCES biography(id),
  status
);