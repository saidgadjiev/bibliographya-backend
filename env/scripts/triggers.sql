CREATE OR REPLACE FUNCTION biography_updated() RETURNS TRIGGER
AS '
BEGIN
  NEW.updated_at := current_date;
  RETURN NEW;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER trigger_biography_updated
BEFORE UPDATE ON biography
FOR EACH ROW
EXECUTE PROCEDURE biography_updated();