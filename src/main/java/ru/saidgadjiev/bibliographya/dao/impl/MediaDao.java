package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Media;
import ru.saidgadjiev.bibliographya.domain.MediaLink;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * Created by said on 16/04/2019.
 */
@Repository
public class MediaDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MediaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Media media) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO media(path) VALUES(?) ON CONFLICT DO NOTHING", Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, media.getPath());

                    return preparedStatement;
                },
                keyHolder
        );

        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            media.setId(((Number) keyHolder.getKeys().get("id")).intValue());
        }
    }

    public void createLink(MediaLink mediaLink) {
        jdbcTemplate.update(
                "INSERT INTO media_link(object_id, media_path) VALUES(?, ?)",
                ps -> {
                    ps.setInt(1, mediaLink.getObjectId());
                    ps.setString(2, mediaLink.getMediaPath());
                }
        );
    }

    public List<MediaLink> getLinks(int objectId) {
        return jdbcTemplate.query(
                "SELECT * FROM media_link WHERE object_id =" + objectId,
                (rs, rowNum) -> {
                    MediaLink mediaLink = new MediaLink();

                    mediaLink.setId(rs.getInt("id"));
                    mediaLink.setObjectId(rs.getInt("object_id"));
                    mediaLink.setMediaPath(rs.getString("media_path"));

                    return mediaLink;
                }
        );
    }

    public List<Media> getNonLinked() {
        return jdbcTemplate.query(
                "SELECT m.id, m.path FROM media m LEFT JOIN media_link ml ON m.path = ml.media_path WHERE ml.media_path IS NULL",
                (rs, rowNum) -> {
                    Media media = new Media();

                    media.setId(rs.getInt("id"));
                    media.setPath(rs.getString("path"));

                    return media;
                }
        );
    }

    public void deleteById(int mediaId) {
        jdbcTemplate.update(
                "DELETE FROM media WHERE id =" + mediaId
        );
    }

    public void removeLinkById(String mediaPath, int objectId) {
        jdbcTemplate.update(
                "DELETE FROM media_link WHERE id IN (SELECT id FROM media_link WHERE media_path = ? AND object_id = ? LIMIT 1)",
                ps -> {
                    ps.setString(1, mediaPath);
                    ps.setInt(2, objectId);
                }
        );
    }
}
