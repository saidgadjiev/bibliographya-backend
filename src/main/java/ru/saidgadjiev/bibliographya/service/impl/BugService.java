package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.bussiness.bug.*;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterCriteriaVisitor;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.sql.SQLException;
import java.util.*;

@Service
public class BugService {

    private final BugDao bugDao;

    private final SecurityService securityService;

    private final Map<Bug.BugStatus, Handler> handlerMap;

    @Autowired
    public BugService(BugDao bugDao, SecurityService securityService, Map<Bug.BugStatus, Handler> handlerMap) {
        this.bugDao = bugDao;
        this.securityService = securityService;
        this.handlerMap = handlerMap;
    }

    public Bug create(BugRequest bugRequest) {
        Bug bug = new Bug();

        bug.setTheme(bugRequest.getTheme());
        bug.setBugCase(bugRequest.getBugCase());

        return bugDao.create(bug);
    }

    public CompleteResult<Bug> complete(int bugId, CompleteRequest completeRequest) throws SQLException {
        Bug updated = doComplete(bugId, completeRequest);

        return new CompleteResult<>(updated == null ? 0 : 1, updated);
    }

    public Collection<BugAction> getActions(Bug bug) {
        User user = (User) securityService.findLoggedInUser();

        return handlerMap.get(bug.getStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("user", user);
                    put("fixerId", bug.getFixerId());
                    put("bugStatus", bug.getStatus());
                }}
        );
    }

    public Page<Bug> getBugsTracks(OffsetLimitPageRequest pageRequest, String query) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node node = new RSQLParser().parse(query);

            node.accept(new FilterCriteriaVisitor<>(criteria, new HashMap<String, FilterCriteriaVisitor.Type>() {{
                put("status", FilterCriteriaVisitor.Type.INTEGER);
                put("fixer_id", FilterCriteriaVisitor.Type.INTEGER);
            }}));
        }

        List<Bug> bugs = bugDao.getList(
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                Sort.by(Sort.Order.asc("created_at")),
                criteria,
                Collections.singleton("fixer")
        );

        return new PageImpl<>(bugs, pageRequest, bugs.size());
    }

    public Page<Bug> getBugs(OffsetLimitPageRequest pageRequest, String query) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node node = new RSQLParser().parse(query);

            node.accept(new FilterCriteriaVisitor<>(criteria, new HashMap<String, FilterCriteriaVisitor.Type>() {{
                put("status", FilterCriteriaVisitor.Type.INTEGER);
            }}));
        }

        List<Bug> bugs = bugDao.getList(
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                Sort.by(Sort.Order.asc("created_at")),
                criteria,
                Collections.emptySet()
        );

        return new PageImpl<>(bugs, pageRequest, bugs.size());
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
