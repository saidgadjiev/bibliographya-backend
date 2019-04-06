CREATE OR REPLACE FUNCTION biography_update()
  RETURNS TRIGGER
AS '
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;'
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_biography_update ON biography;

CREATE TRIGGER trigger_biography_update
  BEFORE UPDATE OF first_name, last_name, middle_name, bio ON biography
  FOR EACH ROW
EXECUTE PROCEDURE biography_update();