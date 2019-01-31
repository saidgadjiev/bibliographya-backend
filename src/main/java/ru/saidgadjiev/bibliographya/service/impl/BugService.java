package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;

@Service
public class BugService {

    private final BugDao bugDao;

    @Autowired
    public BugService(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    public void create(BugRequest bugRequest) {
        Bug bug = new Bug();

        bug.setTheme(bugRequest.getTheme());
        bug.setBugCase(bugRequest.getBugCase());

        bugDao.create(bug);
    }

    public CompleteResult<Biography, ModerationAction> complete(int bugId, CompleteRequest completeRequest) {
        return null;
    }
}
