CREATE TABLE IF NOT EXISTS biography (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(512) NOT NULL,
  last_name VARCHAR(512) NOT NULL,
  middle_name VARCHAR(512) NOT NULL,
  creator_name VARCHAR(128) NOT NULL REFERENCES "user"(name),
  user_name VARCHAR(128) UNIQUE REFERENCES "user"(name),
  biography TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  moderation_status INTEGER NOT NULL DEFAULT 0,
  moderation_info TEXT,
  moderated_at TIMESTAMP,
  moderator_name VARCHAR(128)
);