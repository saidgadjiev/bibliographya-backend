package ru.saidgadjiev.bibliographya.dao.dialect;

public class H2Dialect implements Dialect {
    @Override
    public boolean supportReturning() {
        return false;
    }

    @Override
    public boolean supportOnConflict() {
        return false;
    }
}
