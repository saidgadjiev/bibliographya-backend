package ru.saidgadjiev.bibliographya.bussiness.bug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PendingHandlerTest extends BaseBugBusinessTest {

    @Test
    void handleAssignMe() throws SQLException {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case) VALUES('Тест', 'Тест')"
        );

        PendingHandler pendingHandler = new PendingHandler(bugDao);

        pendingHandler.handle(Handler.Signal.ASSIGN_ME, new HashMap<String, Object>() {{
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
    void handleClose() throws SQLException {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );

        PendingHandler pendingHandler = new PendingHandler(bugDao);

        pendingHandler.handle(Handler.Signal.CLOSE, new HashMap<String, Object>() {{
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
        expected.setStatus(Bug.BugStatus.CLOSED);
        expected.setFixerId(1);

        Assertions.assertNotNull(bug);
        assertEquals(expected, bug);
    }

    @Test
    void handleIgnore() throws SQLException {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );

        PendingHandler pendingHandler = new PendingHandler(bugDao);

        pendingHandler.handle(Handler.Signal.IGNORE, new HashMap<String, Object>() {{
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
        expected.setStatus(Bug.BugStatus.IGNORED);
        expected.setFixerId(1);

        Assertions.assertNotNull(bug);
        assertEquals(expected, bug);
    }

    @Test
    void handleRelease() throws SQLException {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 2)"
        );

        PendingHandler pendingHandler = new PendingHandler(bugDao);

        pendingHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
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
    void getNotAssignedBugActions() {
        createUser();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case) VALUES('Тест', 'Тест')"
        );


        PendingHandler pendingHandler = new PendingHandler(bugDao);

        Collection<BugAction> actions = pendingHandler.getActions(new HashMap<String, Object>() {{
            User user = new User();

            user.setId(1);

            put("user", user);
        }});

        Assertions.assertIterableEquals(Collections.singletonList(BugAction.assignMe()), actions);
    }

    @Test
    void getAssignedBugActions() {
        createUser(ProviderType.FACEBOOK);
        createUserBiography(1);

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );


        PendingHandler pendingHandler = new PendingHandler(bugDao);

        Collection<BugAction> actions = pendingHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);
            User user = new User();

            user.setId(1);

            put("user", user);
        }});

        Assertions.assertIterableEquals(Arrays.asList(BugAction.close(), BugAction.ignore(), BugAction.release()), actions);
    }

    private void assertEquals(Bug expected, Bug actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getStatus(), actual.getStatus());
        Assertions.assertEquals(expected.getFixerId(), actual.getFixerId());
        Assertions.assertEquals(expected.getInfo(), actual.getInfo());
    }
}