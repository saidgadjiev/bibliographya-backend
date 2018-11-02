CREATE TABLE IF NOT EXISTS "user" (
  id SERIAL PRIMARY KEY,
  name VARCHAR(128) UNIQUE NOT NULL,
  password VARCHAR(1024) NOT NULL
);

INSERT INTO "user"("name", "password") VALUES('admin', '$2a$10$MItPOf2gYp7D5MOTw.Jl7O8.NTOtxvpQiR65apQ04QRonrMjQdTKe');
INSERT INTO "user"("name", "password") VALUES('test', '$2a$10$mXyMQeOzYCyBWdCwi42NGeVegfdl2H7uBIXRSFMkG02t5FEeGo8X6');

