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
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ClosedIgnoredHandlerTest {

    @MockBean
    private PendingOperation pendingOperation;

    @MockBean
    private ReleaseOperation releaseOperation;

    @Autowired
    private ClosedIgnoredHandler closedIgnoredHandler;

    @Test
    void handleRelease() throws SQLException {
        closedIgnoredHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
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
        closedIgnoredHandler.handle(Handler.Signal.PENDING, new HashMap<String, Object>() {{
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
        Collection<BugAction> actions = closedIgnoredHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);

            User user = new User();

            user.setId(1);

            put("user", user);
        }});

        Assertions.assertIterableEquals(Arrays.asList(BugAction.pending(), BugAction.release()), actions);
    }

    @Test
    void getForeignActions() {
        Collection<BugAction> actions = closedIgnoredHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);

            User user = new User();

            user.setId(2);

            put("user", user);
        }});

        Assertions.assertTrue(actions.isEmpty());
    }
}