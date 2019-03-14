package ru.saidgadjiev.bibliographya.utils;

import org.springframework.jdbc.core.JdbcTemplate;

public class TableUtils {

    private TableUtils() {

    }

    public static void createTableUser(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"user\" (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  created_at TIMESTAMP(3) DEFAULT NOW(),\n" +
                        "  password VARCHAR(1024) NOT NULL,\n" +
                        "  email VARCHAR(512) NOT NULL,\n" +
                        "  email_verified BOOLEAN DEFAULT FALSE,\n" +
                        "  deleted BOOLEAN NOT NULL DEFAULT FALSE,\n" +
                        "  UNIQUE (email, email_verified)\n" +
                        ")");
    }

    public static void deleteTableUser(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS \"user\""
        );
    }

    public static void createTableBiography(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography (\n" +
                        "  id                SERIAL PRIMARY KEY,\n" +
                        "  first_name        VARCHAR(512) NOT NULL,\n" +
                        "  last_name         VARCHAR(512) NOT NULL,\n" +
                        "  middle_name       VARCHAR(512),\n" +
                        "  creator_id        INTEGER      NOT NULL REFERENCES \"user\" (id),\n" +
                        "  user_id           INTEGER UNIQUE REFERENCES \"user\" (id),\n" +
                        "  biography         TEXT,\n" +
                        "  created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),\n" +
                        "  updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),\n" +
                        "  moderation_status INTEGER      NOT NULL DEFAULT 0,\n" +
                        "  moderation_info   TEXT,\n" +
                        "  moderated_at      TIMESTAMP,\n" +
                        "  moderator_id      INTEGER REFERENCES \"user\" (id),\n" +
                        "  publish_status    INTEGER      NOT NULL DEFAULT 0\n" +
                        ")"
        );
    }

    public static void deleteTableBiography(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography"
        );
    }

    public static void createUserRoleTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS user_role (\n" +
                "  id SERIAL PRIMARY KEY,\n" +
                "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                "  role_name VARCHAR (255) NOT NULL REFERENCES role(name),\n" +
                "  UNIQUE (user_id, role_name)\n" +
                ")");
    }

    public static void createRoleTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS role (\n" +
                "  id SERIAL PRIMARY KEY,\n" +
                "  name VARCHAR(255) NOT NULL UNIQUE\n" +
                ")");
    }

    public static void deleteTableRole(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS role"
        );
    }
}
