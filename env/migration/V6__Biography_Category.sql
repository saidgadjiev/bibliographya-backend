CREATE TABLE IF NOT EXISTS biography_category (
  id SERIAL PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE,
  image_path VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS biography_category_biography (
  id SERIAL PRIMARY KEY,
  category_id INTEGER NOT NULL REFERENCES biography_category(id) ON DELETE CASCADE,
  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,
  UNIQUE (category_id, biography_id)
);
