package ru.saidgadjiev.bibliographya.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by said on 25.11.2018.
 */
public interface PreparedSetter<T> {

    void set(PreparedStatement preparedStatement, int index) throws SQLException;
}
