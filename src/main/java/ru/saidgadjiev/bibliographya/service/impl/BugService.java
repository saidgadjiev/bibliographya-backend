package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.bussiness.bug.*;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class BugService {

    private final BugDao bugDao;

    private final SecurityService securityService;

    private Map<Bug.BugStatus, Handler> handlerMap = new HashMap<>();

    @Autowired
    public BugService(BugDao bugDao, SecurityService securityService) {
        this.bugDao = bugDao;
        this.securityService = securityService;

        initHandlers();
    }

    private void initHandlers() {
        handlerMap.put(Bug.BugStatus.PENDING, new PendingHandler(bugDao));
        handlerMap.put(Bug.BugStatus.IGNORED, new IgnoredHandler(bugDao));
        handlerMap.put(Bug.BugStatus.CLOSED, new ClosedHandler(bugDao));
    }

    public Bug create(BugRequest bugRequest) {
        Bug bug = new Bug();

        bug.setTheme(bugRequest.getTheme());
        bug.setBugCase(bugRequest.getBugCase());

        return bugDao.create(bug);
    }

    public CompleteResult<Bug, BugAction> complete(int bugId, CompleteRequest completeRequest) throws SQLException {
        Bug updated = doComplete(bugId, completeRequest);

        return new CompleteResult<>(1, updated, getActions(updated));
    }

    public Collection<BugAction> getActions(Bug bug) {
        User user = (User) securityService.findLoggedInUser();

        return handlerMap.get(bug.getStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("user", user);
                    put("userId", bug.getFixerId());
                    put("bugStatus", bug.getStatus());
                }}
        );
    }

    public Bug getFixerInfo(int bugId) {
        return bugDao.getFixerInfo(bugId);
    }

    private Bug doComplete(int bugId, CompleteRequest completeRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Map<String, Object> processValues = new HashMap<>();

        processValues.put("bugId", bugId);
        processValues.put("fixerId", userDetails.getId());
        processValues.put("info", completeRequest.getInfo());

        Handler handler = handlerMap.get(
                Bug.BugStatus.fromCode(completeRequest.getStatus())
        );

        Bug updated = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (updated == null) {
            return null;
        }

        if (updated.getFixerId() != null) {
            updated.setFixer(userDetails.getBiography());
        }

        return updated;
    }
}
