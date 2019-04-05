package ru.saidgadjiev.bibliographya.utils;

import org.springframework.jdbc.core.JdbcTemplate;

public class TableUtils {

    private TableUtils() { }

    public static void createBugTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS bug (\n" +
                        "  id       SERIAL PRIMARY KEY,\n" +
                        "  created_at TIMESTAMP NOT NULL DEFAULT now(),\n" +
                        "  fixed_at TIMESTAMP,\n" +
                        "  theme    TEXT NOT NULL,\n" +
                        "  bug_case TEXT NOT NULL,\n" +
                        "  fixer_id INTEGER REFERENCES \"user\" (id),\n" +
                        "  status   INTEGER DEFAULT 0,\n" +
                        "  info     TEXT\n" +
                        ")"
        );
    }

    public static void deleteTableBug(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS bug"
        );
    }

    public static void createTableBiographyLike(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography_like (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                        " created_at TIMESTAMP(3) NOT NULL DEFAULT now(),\n" +
                        "  UNIQUE (biography_id, user_id)\n" +
                        ")"
        );
    }

    public static void deleteTableBiographyLike(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography_like"
        );
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
                        "  publish_status    INTEGER      NOT NULL DEFAULT 0,\n" +
                        " disable_comments BOOLEAN DEFAULT FALSE,\n" +
                        " anonymous_creator BOOLEAN DEFAULT FALSE\n" +
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

    public static void deleteTableUserRole(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS user_role"
        );
    }

    public static void deleteTableRole(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS role"
        );
    }

    public static void createTableBiographyCategory(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography_category (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  name VARCHAR(128) NOT NULL UNIQUE,\n" +
                        "  image_path VARCHAR(128) NOT NULL\n" +
                        ")"
        );
    }

    public static void deleteTableBiographyCategory(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography_category"
        );
    }

    public static void createTableBiographyComment(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography_comment (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  content TEXT NOT NULL,\n" +
                        "  created_at TIMESTAMP(3) NOT NULL DEFAULT NOW(),\n" +
                        "  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                        "  parent_id INTEGER,\n" +
                        "  parent_user_id INTEGER REFERENCES \"user\"(id)\n" +
                        ")"
        );
    }

    public static void deleteTableBiographyComment(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography_comment"
        );
    }
}
