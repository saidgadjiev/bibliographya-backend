CREATE TABLE IF NOT EXISTS biography (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(512) NOT NULL,
  last_name VARCHAR(512) NOT NULL,
  middle_name VARCHAR(512),
  creator_id INTEGER NOT NULL REFERENCES "user"(id),
  user_id INTEGER UNIQUE REFERENCES "user"(id),
  biography TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  moderation_status INTEGER NOT NULL DEFAULT 0,
  moderation_info TEXT,
  moderated_at TIMESTAMP,
  moderator_id INTEGER REFERENCES "user"(id),
  publish_status INTEGER
);