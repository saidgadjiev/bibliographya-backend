package ru.saidgadjiev.bibliographya.bussiness.bug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.*;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PendingHandlerTest {

    @Autowired
    private PendingHandler pendingHandler;

    @MockBean
    private PendingOperation pendingOperation;

    @MockBean
    private AssignMeOperation assignMeOperation;

    @MockBean
    private IgnoreOperation ignoreOperation;

    @MockBean
    private ReleaseOperation releaseOperation;

    @MockBean
    private CloseOperation closeOperation;

    @Test
    void handleAssignMe() throws SQLException {
        pendingHandler.handle(Handler.Signal.ASSIGN_ME, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Mockito.verify(assignMeOperation, Mockito.times(1)).execute(new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});
    }

    @Test
    void handleClose() throws SQLException {
        pendingHandler.handle(Handler.Signal.CLOSE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Mockito.verify(closeOperation, Mockito.times(1)).execute(new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});
    }

    @Test
    void handleIgnore() throws SQLException {
        pendingHandler.handle(Handler.Signal.IGNORE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Mockito.verify(ignoreOperation, Mockito.times(1)).execute(new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});
    }

    @Test
    void handleRelease() throws SQLException {
        pendingHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});

        Mockito.verify(releaseOperation, Mockito.times(1)).execute(new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
        }});
    }

    @Test
    void getNotAssignedBugActions() {
        Collection<BugAction> actions = pendingHandler.getActions(new HashMap<String, Object>() {{
            User user = new User();

            user.setId(1);

            put("user", user);
        }});

        Assertions.assertIterableEquals(Collections.singletonList(BugAction.assignMe()), actions);
    }

    @Test
    void getAssignedBugActions() {
        Collection<BugAction> actions = pendingHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);
            User user = new User();

            user.setId(1);

            put("user", user);
        }});

        Assertions.assertIterableEquals(Arrays.asList(BugAction.close(), BugAction.ignore(), BugAction.release()), actions);
    }
}