package ru.saidgadjiev.bibliographya.bussiness.common;

import java.sql.SQLException;
import java.util.Map;

public interface BusinessOperation<T> {

    T execute(Map<String, Object> args) throws SQLException;
}
