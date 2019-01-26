package ru.saidgadjiev.bibliographya.bussiness.fix.operation;

import ru.saidgadjiev.bibliographya.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliographya.bussiness.fix.Handler;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;

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
