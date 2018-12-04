CREATE TABLE IF NOT EXISTS biography_category_biography (
  id SERIAL PRIMARY KEY,
  category_name VARCHAR(128) NOT NULL REFERENCES biography_category(name) ON UPDATE CASCADE,
  biography_id INTEGER NOT NULL REFERENCES biography(id),
  UNIQUE (category_name, biography_id)
);