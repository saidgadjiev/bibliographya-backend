package ru.saidgadjiev.bibliographya.bussiness.bug.operation;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.bussiness.bug.BaseBugBusinessTest;
import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.util.Collections;
import java.util.Map;

public abstract class BaseBugOperationTest extends BaseBugBusinessTest {

    @Test
    void execute() {
        createUser(ProviderType.FACEBOOK);
        createUserBiography(1);

        preExecute();
        bugBusinessOperation().execute(args());

        Bug bug = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );

        assertEquals(expected(), bug);
    }

    protected abstract BusinessOperation<Bug> bugBusinessOperation();

    protected Map<String, Object> args() {
        return Collections.emptyMap();
    }

    protected void preExecute() {

    }

    protected abstract Bug expected();

    protected void assertEquals(Bug expected, Bug actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getStatus(), actual.getStatus());
        Assertions.assertEquals(expected.getFixerId(), actual.getFixerId());
        Assertions.assertEquals(expected.getInfo(), actual.getInfo());
    }
}
