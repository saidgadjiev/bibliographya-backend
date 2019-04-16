package ru.saidgadjiev.bibliographya.dao.impl;

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

    public void create(Media media) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO media(path) VALUES(?)", Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, media.getPath());

                    return preparedStatement;
                },
                keyHolder
        );

        if (keyHolder.getKeys().containsKey("id")) {
            media.setId(((Number) keyHolder.getKeys().get("id")).intValue());
        }
    }

    public void createLink(MediaLink mediaLink) {
        jdbcTemplate.update(
                "INSERT INTO media_link(object_id, media_id) VALUES(?, ?)",
                ps -> {
                    ps.setInt(1, mediaLink.getObjectId());
                    ps.setInt(2, mediaLink.getMediaId());
                }
        );
    }

    public List<MediaLink> getAllLinks() {
        return jdbcTemplate.query(
                "SELECT object_id, media_id FROM media_link ORDER BY created_at ASC",
                (rs, rowNum) -> {
                    MediaLink mediaLink = new MediaLink();

                    mediaLink.setId(rs.getInt("id"));
                    mediaLink.setObjectId(rs.getInt("object_id"));
                    mediaLink.setMediaId(rs.getInt("media_id"));

                    return mediaLink;
                }
        );
    }

    public List<Media> getNonLinked() {
        return jdbcTemplate.query(
                "SELECT m.id, m.path FROM media m WHERE NOT EXISTS (SELECT 1 FROM media_link WHERE media_id = m.id)",
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
}
