CREATE TABLE IF NOT EXISTS biography_category (
  id SERIAL PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE,
  image_path VARCHAR(128) NOT NULL
);

INSERT INTO biography_category(name, image_path)
VALUES
  ('Поэты', 'Poets.jpg');

INSERT INTO biography_category(name, image_path)
VALUES
  ('Композиторы', 'Сomposers.jpg');

INSERT INTO biography_category(name, image_path)
VALUES
  ('Актеры', 'Actors.jpg');

INSERT INTO biography_category(name, image_path)
VALUES
  ('Художники', 'Painters.jpg');

INSERT INTO biography_category(name, image_path)
VALUES
  ('Спортсмены', 'Sportsmen.jpg');

INSERT INTO biography_category(name, image_path)
VALUES
  ('Правители', 'Rulers.jpg');
