package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.impl.dsl.DslVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GeneralDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public GeneralDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public KeyHolder create(String table, Collection<UpdateValue> values) {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO \"").append(table).append("\"(");

        StringBuilder valuesBuilder = new StringBuilder();

        for (Iterator<UpdateValue> iterator = values.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName());
            valuesBuilder.append("?");

            if (iterator.hasNext()) {
                sql.append(", ");
                valuesBuilder.append(",");
            }
        }

        sql.append(") VALUES(");

        sql.append(valuesBuilder.toString());

        sql.append(") ");

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

                    int i = 0;

                    for (UpdateValue updateValue : values) {
                        updateValue.getSetter().set(ps, ++i);
                    }

                    return ps;
                },
                keyHolder
        );

        return keyHolder;
    }

    public int update(String table, Collection<UpdateValue> values, AndCondition criteria, List<PreparedSetter> setValues, Map<String, Object> returnValues) {
        DslVisitor visitor = new DslVisitor(null);

        criteria.accept(visitor);

        String clause = visitor.getClause();
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE \"").append(table).append("\" SET ");

        for (Iterator<UpdateValue> iterator = values.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName()).append(" = ?");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause).append(" ");
        }

        if (returnValues != null && !returnValues.isEmpty()) {
            String returning = returnValues.keySet().stream().collect(Collectors.joining(", "));

            sql.append("RETURNING ").append(returning);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int update = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

                    int i = 0;

                    for (UpdateValue updateValue : values) {
                        updateValue.getSetter().set(ps, ++i);
                    }
                    for (PreparedSetter preparedSetter: setValues) {
                        preparedSetter.set(ps, ++i);
                    }

                    return ps;
                },
                keyHolder
        );

        if (returnValues != null && !returnValues.isEmpty()) {
            returnValues.putAll(keyHolder.getKeys());
        }

        return update;
    }

    public List<Map<String, Object>> getFields(String table, Collection<String> fields, AndCondition criteria, List<PreparedSetter> values) {
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

        DslVisitor visitor = new DslVisitor(null);

        criteria.accept(visitor);

        String clause = visitor.getClause();

        if (StringUtils.isNotBlank(clause)) {
            builder.append("WHERE ").append(clause);
        }

        return jdbcTemplate.query(
                builder.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(ps, ++i);
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

    public Map<String, Object> uniqueValue(String table, Collection<String> fields, AndCondition criteria, List<PreparedSetter> vals) {
        List<Map<String, Object>> values = getFields(table, fields, criteria, vals);

        return values.isEmpty() ? null : values.iterator().next();
    }
}
