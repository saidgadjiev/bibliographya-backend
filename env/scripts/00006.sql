CREATE TABLE IF NOT EXISTS biography_category (
  id SERIAL PRIMARY KEY,
  email VARCHAR(128) NOT NULL UNIQUE,
  image_path VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS biography_category_biography (
  id SERIAL PRIMARY KEY,
  category_id INTEGER NOT NULL REFERENCES biography_category(id) ON DELETE CASCADE,
  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,
  UNIQUE (category_id, biography_id)
);

INSERT INTO biography_category(email, image_path)
VALUES
  ('Поэты', '1.jpg');

INSERT INTO biography_category(email, image_path)
VALUES
  ('Композиторы', '2.jpg');

INSERT INTO biography_category(email, image_path)
VALUES
  ('Актеры', '3.jpg');

INSERT INTO biography_category(email, image_path)
VALUES
  ('Художники', '4.jpg');

INSERT INTO biography_category(email, image_path)
VALUES
  ('Спортсмены', '5.jpg');

INSERT INTO biography_category(email, image_path)
VALUES
  ('Правители', '6.jpg');
