package ru.saidgadjiev.bibliographya.bussiness.fix;

import ru.saidgadjiev.bibliographya.domain.BiographyFix;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by said on 21.12.2018.
 */
public class EmptyHandler implements Handler {

    @Override
    public BiographyFix handle(Signal signal, Map<String, Object> args) throws SQLException {
        return null;
    }

    @Override
    public Collection<FixAction> getActions(Map<String, Object> args) {
        return Collections.emptyList();
    }
}
