package ru.saidgadjiev.bibliographya.bussiness.bug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.PendingOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.ReleaseOperation;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ClosedHandlerTest {

    @MockBean
    private PendingOperation pendingOperation;

    @MockBean
    private ReleaseOperation releaseOperation;

    @Autowired
    private ClosedHandler closedHandler;

    @Test
    void handleRelease() throws SQLException {
        closedHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Mockito.verify(releaseOperation, Mockito.times(1)).execute(new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});
    }


    @Test
    void handlePending() throws SQLException {
        closedHandler.handle(Handler.Signal.PENDING, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Mockito.verify(pendingOperation, Mockito.times(1)).execute(new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});
    }

    @Test
    void getForMyBugActions() {
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