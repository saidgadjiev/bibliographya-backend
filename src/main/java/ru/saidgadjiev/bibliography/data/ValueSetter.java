package ru.saidgadjiev.bibliography.data;

import java.sql.PreparedStatement;

/**
 * Created by said on 24.11.2018.
 */
public interface ValueSetter<T> {

    void set(PreparedStatement statement, T value);
}
