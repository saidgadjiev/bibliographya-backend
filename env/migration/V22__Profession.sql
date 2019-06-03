CREATE TABLE IF NOT EXISTS profession
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(512) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS biography_profession
(
    id            SERIAL PRIMARY KEY,
    biography_id  INTEGER REFERENCES biography (id) ON DELETE CASCADE,
    profession_id INTEGER REFERENCES profession (id) ON DELETE CASCADE
);

INSERT INTO profession
VALUES ('Программист'),
       ('Предприниматель'),
       ('Изобретатель'),
       ('Модельер'),
       ('Дизайнер'),
       ('Актер'),
       ('Актриса'),
       ('Боксер'),
       ('Ученный'),
       ('Футболлист'),
       ('Модель'),
       ('Писатель'),
       ('Поэт')
ON CONFLICT(name) DO NOTHING;
