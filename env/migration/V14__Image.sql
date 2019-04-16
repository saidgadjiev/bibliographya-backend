CREATE TABLE IF NOT EXISTS media (
  id SERIAL PRIMARY KEY,
  path VARCHAR(512) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS media_links (
  id SERIAL PRIMARY KEY,
  object_id INTEGER NOT NULL,
  media_id INT NOT NULL
);