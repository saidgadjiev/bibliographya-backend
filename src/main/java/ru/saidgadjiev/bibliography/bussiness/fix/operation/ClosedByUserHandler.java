package ru.saidgadjiev.bibliography.bussiness.fix.operation;

import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliography.bussiness.fix.Handler;
import ru.saidgadjiev.bibliography.domain.BiographyFix;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 31.12.2018.
 */
public class ClosedByUserHandler implements Handler {

    @Override
    public BiographyFix handle(Signal signal, Map<String, Object> args) throws SQLException {
        return null;
    }

    @Override
    public Collection<FixAction> getActions(Map<String, Object> args) {
        return null;
    }
}
