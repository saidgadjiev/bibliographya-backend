package ru.saidgadjiev.bibliographya.dao.dialect;

public class PostgresDialect implements Dialect {
    @Override
    public boolean supportReturning() {
        return true;
    }
}
