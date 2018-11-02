CREATE TABLE IF NOT EXISTS biography (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(512) NOT NULL,
  last_name VARCHAR(512) NOT NULL,
  middle_name VARCHAR(512) NOT NULL,
  creator_name VARCHAR(128) NOT NULL REFERENCES "user"(name),
  user_name VARCHAR(128) UNIQUE REFERENCES "user"(name),
  biography TEXT
);
