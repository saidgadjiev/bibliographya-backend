CREATE TABLE IF NOT EXISTS biography_view_count (
  id SERIAL PRIMARY KEY,
  biography_id INT NOT NULL UNIQUE REFERENCES biography(id),
  views_count BIGINT NOT NULL DEFAULT 0
)