package ru.saidgadjiev.bibliography.bussiness.complaint;

import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by said on 01.01.2019.
 */
public class ConsideredHandler implements Handler {

    @Override
    public void handle(Signal signal, Map<String, Object> args) throws SQLException {
    }

    @Override
    public Collection<ComplaintAction> getActions(Map<String, Object> args) {
        return Collections.emptyList();
    }
}
