package ru.saidgadjiev.bibliographya.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by said on 28.12.2018.
 */
public class ResultSetUtils {

    private ResultSetUtils() { }

    public static Integer intOrNull(ResultSet rs, String column) throws SQLException {
        Integer result = rs.getInt(column);

        if (rs.wasNull()) {
            return null;
        }

        return result;
    }
}
