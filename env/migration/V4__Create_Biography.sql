CREATE TABLE IF NOT EXISTS biography (
  id                SERIAL PRIMARY KEY,
  first_name        VARCHAR(512) NOT NULL,
  last_name         VARCHAR(512) NOT NULL,
  middle_name       VARCHAR(512),
  creator_id        INTEGER      NOT NULL REFERENCES "user" (id),
  user_id           INTEGER UNIQUE REFERENCES "user" (id),
  biography         TEXT,
  created_at        TIMESTAMP(3)    NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMP(3)    NOT NULL DEFAULT NOW(),
  moderation_status INTEGER      NOT NULL DEFAULT 0,
  moderation_info   TEXT,
  moderated_at      TIMESTAMP(3),
  moderator_id      INTEGER REFERENCES "user" (id),
  publish_status    INTEGER      NOT NULL DEFAULT 0
);

CREATE OR REPLACE FUNCTION biography_update()
  RETURNS TRIGGER
AS '
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER trigger_biography_update
BEFORE UPDATE ON biography
FOR EACH ROW
EXECUTE PROCEDURE biography_update();

INSERT INTO biography (first_name, last_name, middle_name, creator_id, user_id,  moderation_status, moderated_at, moderator_id)
VALUES ('Саид', 'Гаджиев', 'Алиевич', 1, 1, 1, now(), 1);