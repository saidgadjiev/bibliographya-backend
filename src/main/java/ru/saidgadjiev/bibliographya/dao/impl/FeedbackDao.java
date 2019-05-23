package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Feedback;

import java.util.List;

@Repository
public class FeedbackDao {

    private JdbcTemplate jdbcTemplate;

    public FeedbackDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Feedback feedback) {
        jdbcTemplate.update(
                "INSERT INTO feedback(content) VALUES (?)",
                preparedStatement -> preparedStatement.setString(1, feedback.getContent())
        );
    }

    public List<Feedback> getList(int limit, long offset) {
        return jdbcTemplate.query(
                "SELECT * FROM feedback LIMIT " + limit + " OFFSET " + offset,
                (resultSet, i) -> {
                    Feedback feedback = new Feedback();

                    feedback.setId(resultSet.getInt("id"));
                    feedback.setContent(resultSet.getString("content"));

                    return feedback;
                }
        );
    }
}
