package ru.saidgadjiev.bibliographya.bussiness.bug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

class ClosedHandlerTest extends BaseBugBusinessTest {

    @Test
    void handleRelease() throws SQLException {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 1)"
        );

        ClosedHandler closedHandler = new ClosedHandler(bugDao);

        closedHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Bug bug = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );

        Bug expected = new Bug();

        expected.setId(1);
        expected.setStatus(Bug.BugStatus.PENDING);

        Assertions.assertNotNull(bug);
        assertEquals(expected, bug);
    }


    @Test
    void handlePending() throws SQLException {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 1)"
        );

        ClosedHandler closedHandler = new ClosedHandler(bugDao);

        closedHandler.handle(Handler.Signal.PENDING, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Bug bug = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );

        Bug expected = new Bug();

        expected.setId(1);
        expected.setStatus(Bug.BugStatus.PENDING);
        expected.setFixerId(1);

        Assertions.assertNotNull(bug);
        assertEquals(expected, bug);
    }

    @Test
    void getForMyBugActions() {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 1)"
        );

        ClosedHandler closedHandler = new ClosedHandler(bugDao);

        Collection<BugAction> actions = closedHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);

            User user = new User();

            user.setId(1);

            put("user", user);
        }});

        Assertions.assertIterableEquals(Arrays.asList(BugAction.pending(), BugAction.release()), actions);
    }

    @Test
    void getForeignActions() {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 1)"
        );

        ClosedHandler closedHandler = new ClosedHandler(bugDao);

        Collection<BugAction> actions = closedHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);

            User user = new User();

            user.setId(2);

            put("user", user);
        }});

        Assertions.assertTrue(actions.isEmpty());
    }

    private void assertEquals(Bug expected, Bug actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getStatus(), actual.getStatus());
        Assertions.assertEquals(expected.getFixerId(), actual.getFixerId());
        Assertions.assertEquals(expected.getInfo(), actual.getInfo());
    }
}