package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.bussiness.bug.*;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BugServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BugService bugService;

    @MockBean
    private BugDao bugDao;

    @MockBean
    private Map<Bug.BugStatus, Handler> handlerMap;

    @MockBean
    private SecurityService securityService;

    private User testUser = new User();

    @BeforeEach
    void init() {
        testUser.setId(1);

        Mockito.when(securityService.findLoggedInUser()).thenReturn(testUser);
    }

    @Test
    void create() {
        List<Bug> db = new ArrayList<>();

        Mockito.when(bugDao.create(any(), any())).thenAnswer(invocationOnMock -> {
            Bug bug = (Bug) invocationOnMock.getArguments()[0];

            bug.setId(1);
            bug.setStatus(Bug.BugStatus.PENDING);
            bug.setCreatedAt(new Timestamp(new Date().getTime()));

            db.add(bug);

            return bug;
        });
        BugRequest request = new BugRequest();

        request.setTheme("Тест");
        request.setBugCase("Тест");

        Bug created = bugService.create(TimeZone.getDefault(), request);

        Assertions.assertEquals(1, db.size());

        Bug bug = db.iterator().next();

        Assertions.assertNotNull(bug);

        Bug expected = createMockBug(Bug.BugStatus.PENDING, null);

        assertEquals(expected, bug, Collections.emptySet());
        assertEquals(created, bug, Collections.emptySet());
    }

    @Test
    void completeAssignMe() throws SQLException {
        PendingHandler pendingHandler = Mockito.mock(PendingHandler.class);

        Bug current = createMockBug(Bug.BugStatus.PENDING, null);

        Mockito.when(handlerMap.get(Bug.BugStatus.PENDING)).thenReturn(pendingHandler);
        Mockito.when(pendingHandler.handle(Handler.Signal.ASSIGN_ME, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
            put("info", null);
        }})).thenAnswer(invocationOnMock -> {
            current.setId(1);
            current.setStatus(Bug.BugStatus.PENDING);
            current.setFixerId(1);

            return current;
        });

        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.ASSIGN_ME.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());

        CompleteResult<Bug> completeResult = bugService.complete(null, 1, completeRequest);

        Bug expected = createMockBug(Bug.BugStatus.PENDING, 1);

        assertEquals(expected, current, Collections.emptySet());
        assertEquals(completeResult.getObject(), current, Collections.emptySet());
    }

    @Test
    void completeClose() throws SQLException {
        PendingHandler pendingHandler = Mockito.mock(PendingHandler.class);

        Bug current = createMockBug(Bug.BugStatus.PENDING, 1);

        Mockito.when(handlerMap.get(Bug.BugStatus.PENDING)).thenReturn(pendingHandler);
        Mockito.when(pendingHandler.handle(Handler.Signal.CLOSE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
            put("info", null);
        }})).thenAnswer(invocationOnMock -> {
            current.setId(1);
            current.setStatus(Bug.BugStatus.CLOSED);
            current.setFixerId(1);

            return current;
        });
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.CLOSE.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());

        CompleteResult<Bug> completeResult = bugService.complete(null, 1, completeRequest);

        Bug expected = createMockBug(Bug.BugStatus.CLOSED, 1);

        assertEquals(expected, current, Collections.emptySet());
        assertEquals(completeResult.getObject(), current, Collections.emptySet());
    }

    @Test
    void completeIgnore() throws SQLException {
        PendingHandler pendingHandler = Mockito.mock(PendingHandler.class);

        Bug current = createMockBug(Bug.BugStatus.PENDING, 1);

        Mockito.when(handlerMap.get(Bug.BugStatus.PENDING)).thenReturn(pendingHandler);
        Mockito.when(pendingHandler.handle(Handler.Signal.IGNORE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
            put("info", "Тест");
        }})).thenAnswer(invocationOnMock -> {
            current.setId(1);
            current.setStatus(Bug.BugStatus.IGNORED);
            current.setFixerId(1);
            current.setInfo("Тест");

            return current;
        });
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.IGNORE.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());
        completeRequest.setInfo("Тест");

        CompleteResult<Bug> completeResult = bugService.complete(null, 1, completeRequest);

        Bug expected = createMockBug(Bug.BugStatus.IGNORED, 1);

        expected.setInfo("Тест");

        assertEquals(expected, current, Collections.emptySet());
        assertEquals(completeResult.getObject(), current, Collections.emptySet());
    }

    @Test
    void completeReleaseFromIgnored() throws SQLException {
        ClosedIgnoredHandler ignoredHandler = Mockito.mock(ClosedIgnoredHandler.class);

        Bug current = createMockBug(Bug.BugStatus.IGNORED, 1);

        Mockito.when(handlerMap.get(Bug.BugStatus.IGNORED)).thenReturn(ignoredHandler);
        Mockito.when(ignoredHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
            put("info", null);
        }})).thenAnswer(invocationOnMock -> {
            current.setId(1);
            current.setStatus(Bug.BugStatus.PENDING);
            current.setFixerId(null);

            return current;
        });
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.RELEASE.getDesc());
        completeRequest.setStatus(Bug.BugStatus.IGNORED.getCode());

        CompleteResult<Bug> completeResult = bugService.complete(any(), 1, completeRequest);

        Bug expected = createMockBug(Bug.BugStatus.PENDING, null);

        assertEquals(expected, current, Collections.emptySet());
        assertEquals(completeResult.getObject(), current, Collections.emptySet());
    }

    @Test
    void completeReleaseFromClosed() throws SQLException {
        ClosedIgnoredHandler closedIgnoredHandler = Mockito.mock(ClosedIgnoredHandler.class);

        Bug current = createMockBug(Bug.BugStatus.CLOSED, 1);

        Mockito.when(handlerMap.get(Bug.BugStatus.CLOSED)).thenReturn(closedIgnoredHandler);
        Mockito.when(closedIgnoredHandler.handle(Handler.Signal.RELEASE, new HashMap<String, Object>() {{
            put("bugId", 1);
            put("fixerId", 1);
            put("info", null);
        }})).thenAnswer(invocationOnMock -> {
            current.setId(1);
            current.setStatus(Bug.BugStatus.PENDING);
            current.setFixerId(null);

            return current;
        });
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.RELEASE.getDesc());
        completeRequest.setStatus(Bug.BugStatus.CLOSED.getCode());

        CompleteResult<Bug> completeResult = bugService.complete(any(), 1, completeRequest);

        Bug expected = createMockBug(Bug.BugStatus.PENDING, null);

        assertEquals(expected, current, Collections.emptySet());
        assertEquals(completeResult.getObject(), current, Collections.emptySet());
    }

    @Test
    void getPendingActions() {
        PendingHandler pendingHandler = Mockito.mock(PendingHandler.class);
        Mockito.when(handlerMap.get(Bug.BugStatus.PENDING)).thenReturn(pendingHandler);
        Mockito.when(pendingHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", null);
            put("user", testUser);
            put("bugStatus", Bug.BugStatus.PENDING);
        }})).thenReturn(Collections.singletonList(BugAction.assignMe()));

        Bug bug = createMockBug(Bug.BugStatus.PENDING, null);

        Collection<BugAction> actions = bugService.getActions(bug);

        Assertions.assertIterableEquals(Collections.singletonList(BugAction.assignMe()), actions);
    }

    @Test
    void getPendingAssignedActions() {
        PendingHandler pendingHandler = Mockito.mock(PendingHandler.class);
        Mockito.when(handlerMap.get(Bug.BugStatus.PENDING)).thenReturn(pendingHandler);
        Mockito.when(pendingHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);
            put("user", testUser);
            put("bugStatus", Bug.BugStatus.PENDING);
        }})).thenReturn(Arrays.asList(BugAction.close(), BugAction.ignore(), BugAction.release()));

        Bug bug = createMockBug(Bug.BugStatus.PENDING, 1);

        Collection<BugAction> actions = bugService.getActions(bug);

        Assertions.assertIterableEquals(Arrays.asList(BugAction.close(), BugAction.ignore(), BugAction.release()), actions);
    }

    @Test
    void getIgnoredActions() {
        ClosedIgnoredHandler ignoredHandler = Mockito.mock(ClosedIgnoredHandler.class);
        Mockito.when(handlerMap.get(Bug.BugStatus.IGNORED)).thenReturn(ignoredHandler);
        Mockito.when(ignoredHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);
            put("user", testUser);
            put("bugStatus", Bug.BugStatus.IGNORED);
        }})).thenReturn(Arrays.asList(BugAction.pending(), BugAction.release()));

        Bug bug = createMockBug(Bug.BugStatus.IGNORED, 1);

        Collection<BugAction> actions = bugService.getActions(bug);

        Assertions.assertIterableEquals(Arrays.asList(BugAction.pending(), BugAction.release()), actions);
    }

    @Test
    void getClosedActions() {
        ClosedIgnoredHandler closedIgnoredHandler = Mockito.mock(ClosedIgnoredHandler.class);
        Mockito.when(handlerMap.get(Bug.BugStatus.CLOSED)).thenReturn(closedIgnoredHandler);
        Mockito.when(closedIgnoredHandler.getActions(new HashMap<String, Object>() {{
            put("fixerId", 1);
            put("user", testUser);
            put("bugStatus", Bug.BugStatus.CLOSED);
        }})).thenReturn(Arrays.asList(BugAction.pending(), BugAction.release()));

        Bug bug = createMockBug(Bug.BugStatus.CLOSED, 1);

        Collection<BugAction> actions = bugService.getActions(bug);

        Assertions.assertIterableEquals(Arrays.asList(BugAction.pending(), BugAction.release()), actions);
    }

    @Test
    void getFixerInfo() {
        Bug bug = new Bug();

        bug.setId(1);
        bug.setStatus(Bug.BugStatus.CLOSED);
        Biography biography = new Biography();

        biography.setFirstName("Тест");
        biography.setLastName("Тест");

        bug.setFixer(biography);

        Mockito.when(bugDao.getFixerInfo(1)).thenReturn(bug);
        Bug result = bugService.getFixerInfo(1);

        Assertions.assertEquals((int) result.getId(), 1);
        Assertions.assertEquals(result.getStatus(), Bug.BugStatus.CLOSED);
        Assertions.assertEquals(result.getFixer().getFirstName(), "Тест");
        Assertions.assertEquals(result.getFixer().getLastName(), "Тест");
    }

    @Test
    void getBugs() {
        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .build();
        Bug bug1 = new Bug();

        bug1.setId(1);
        bug1.setTheme("Test");
        bug1.setBugCase("Test");
        bug1.setFixerId(1);
        bug1.setStatus(Bug.BugStatus.PENDING);

        Bug bug2 = new Bug();

        bug2.setId(2);
        bug1.setTheme("Test2");
        bug1.setBugCase("Test2");
        bug1.setFixerId(1);
        bug1.setStatus(Bug.BugStatus.PENDING);

        List<Bug> bugs = new ArrayList<>();

        bugs.add(bug1);
        bugs.add(bug2);

        Mockito.when(
                bugDao.getList(
                        any(),
                        eq(10),
                        eq(0L),
                        eq(Sort.by(Sort.Order.asc("created_at"))),
                        eq(Collections.emptyList()),
                        eq(Collections.emptySet())
                )
        ).thenReturn(bugs);

        Page<Bug> page = bugService.getBugs(any(), pageRequest, null);

        Assertions.assertEquals(page.getContent().size(), 2);
        assertEquals(bug1, page.getContent().get(0), Collections.emptySet());
        assertEquals(bug2, page.getContent().get(1), Collections.emptySet());
    }

    @Test
    void getBugsTracks() {
        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .build();
        Bug bug1 = new Bug();

        bug1.setId(1);
        bug1.setTheme("Test");
        bug1.setBugCase("Test");
        bug1.setFixerId(1);
        bug1.setStatus(Bug.BugStatus.PENDING);

        Biography fixer = new Biography();

        fixer.setId(1);
        fixer.setFirstName("Test");
        fixer.setLastName("Test");

        bug1.setFixer(fixer);

        Bug bug2 = new Bug();

        bug2.setId(2);
        bug1.setTheme("Test2");
        bug1.setBugCase("Test2");
        bug1.setFixerId(1);
        bug1.setStatus(Bug.BugStatus.PENDING);

        bug2.setFixer(fixer);

        List<Bug> bugs = new ArrayList<>();

        bugs.add(bug1);
        bugs.add(bug2);

        Mockito.when(
                bugDao.getList(
                        any(),
                        eq(10),
                        eq(0L),
                        eq(Sort.by(Sort.Order.asc("created_at"))),
                        eq(Collections.emptyList()),
                        eq(Collections.singleton("fixer"))
                )
        ).thenReturn(bugs);

        Page<Bug> page = bugService.getBugsTracks(any(), pageRequest, null);

        Assertions.assertEquals(page.getContent().size(), 2);
        assertEquals(bug1, page.getContent().get(0), Collections.singleton("fixer"));
        assertEquals(bug2, page.getContent().get(1), Collections.singleton("fixer"));
    }


    @Test
    void getBugsWithQuery() {
        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .build();
        Bug bug1 = new Bug();

        bug1.setId(1);
        bug1.setTheme("Test");
        bug1.setBugCase("Test");
        bug1.setFixerId(1);
        bug1.setStatus(Bug.BugStatus.PENDING);
        List<Bug> bugs = new ArrayList<>();

        bugs.add(bug1);

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName("status")
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setInt)
                        .filterValue(0)
                        .build()
        );

        Mockito.when(
                bugDao.getList(
                        any(),
                        eq(10),
                        eq(0L),
                        eq(Sort.by(Sort.Order.asc("created_at"))),
                        eq(criteria),
                        eq(Collections.emptySet())
                )
        ).thenReturn(bugs);

        Page<Bug> page = bugService.getBugs(any(), pageRequest, "status==0");

        Assertions.assertEquals(page.getContent().size(), 1);
        assertEquals(bug1, page.getContent().get(0), Collections.emptySet());
    }

    protected void assertEquals(Bug expected, Bug actual, Set<String> fields) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getStatus(), actual.getStatus());
        Assertions.assertEquals(expected.getFixerId(), actual.getFixerId());
        Assertions.assertEquals(expected.getInfo(), actual.getInfo());

        if (fields.contains("fixer")) {
            Assertions.assertEquals(expected.getFixer().getId(), actual.getFixer().getId());
            Assertions.assertEquals(expected.getFixer().getFirstName(), actual.getFixer().getFirstName());
            Assertions.assertEquals(expected.getFixer().getLastName(), actual.getFixer().getLastName());
        }
    }

    private Bug createMockBug(Bug.BugStatus status, Integer fixerId) {
        Bug current = new Bug();

        current.setId(1);
        current.setTheme("Тест");
        current.setBugCase("Тест");
        current.setCreatedAt(new Timestamp(new Date().getTime()));
        current.setStatus(status);
        current.setFixerId(fixerId);

        return current;
    }
}