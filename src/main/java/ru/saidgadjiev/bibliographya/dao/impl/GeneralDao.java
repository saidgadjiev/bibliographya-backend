package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;

import java.sql.ResultSetMetaData;
import java.util.*;

import static ru.saidgadjiev.bibliographya.utils.FilterUtils.toClause;

@Repository
public class GeneralDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public GeneralDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getFields(String table, Collection<String> fields, Collection<FilterCriteria> criteria) {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");

        if (fields.isEmpty()) {
            builder.append("*");
        } else {
            for (Iterator<String> fieldIterator = fields.iterator(); fieldIterator.hasNext(); ) {
                builder.append(fieldIterator.next());

                if (fieldIterator.hasNext()) {
                    builder.append(",");
                }
            }
        }
        builder.append(" ").append("FROM \"").append(table).append("\" ");

        String clause = toClause(criteria, null);

        if (StringUtils.isNotBlank(clause)) {
            builder.append("WHERE ").append(clause);
        }

        return jdbcTemplate.query(
                builder.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }
                },
                rs -> {
                    List<Map<String, Object>> result = new ArrayList<>();
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>(columns);

                        for (int i = 1; i <= columns; ++i) {
                            row.put(md.getColumnName(i), rs.getObject(i));
                        }
                        result.add(row);
                    }

                    return result;
                }
        );
    }
}
