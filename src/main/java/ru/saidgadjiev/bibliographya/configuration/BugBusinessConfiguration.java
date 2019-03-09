package ru.saidgadjiev.bibliographya.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.saidgadjiev.bibliographya.bussiness.bug.ClosedIgnoredHandler;
import ru.saidgadjiev.bibliographya.bussiness.bug.Handler;
import ru.saidgadjiev.bibliographya.bussiness.bug.PendingHandler;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.*;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BugBusinessConfiguration {

    @Bean
    public Map<Bug.BugStatus, Handler> bugHandlerMap(
            PendingHandler pendingHandler,
            ClosedIgnoredHandler closedIgnoredHandler
    ) {
        Map<Bug.BugStatus, Handler> handlerMap = new HashMap<>();

        handlerMap.put(Bug.BugStatus.PENDING, pendingHandler);
        handlerMap.put(Bug.BugStatus.IGNORED, closedIgnoredHandler);
        handlerMap.put(Bug.BugStatus.CLOSED, closedIgnoredHandler);

        return handlerMap;
    }

    @Bean
    public PendingHandler pendingHandler(
            AssignMeOperation assignMeOperation,
            IgnoreOperation ignoreOperation,
            ReleaseOperation releaseOperation,
            CloseOperation closeOperation
    ) {
        return new PendingHandler(
                assignMeOperation, closeOperation, ignoreOperation, releaseOperation
        );
    }

    @Bean
    public ClosedIgnoredHandler closedHandler(
            PendingOperation pendingOperation,
            ReleaseOperation releaseOperation
    ) {
        return new ClosedIgnoredHandler(
                pendingOperation, releaseOperation
        );
    }

    @Bean
    public AssignMeOperation assignMeOperation(BugDao bugDao) {
        return new AssignMeOperation(bugDao);
    }

    @Bean
    public PendingOperation pendingOperation(BugDao bugDao) {
        return new PendingOperation(bugDao);
    }

    @Bean
    public IgnoreOperation ignoreOperation(BugDao bugDao) {
        return new IgnoreOperation(bugDao);
    }

    @Bean
    public ReleaseOperation releaseOperation(BugDao bugDao) {
        return new ReleaseOperation(bugDao);
    }

    @Bean
    public CloseOperation closeOperation(BugDao bugDao) {
        return new CloseOperation(bugDao);
    }
}
