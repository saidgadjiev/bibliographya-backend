package ru.saidgadjiev.bibliography.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.bussiness.fix.ClosedHandler;
import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliography.bussiness.fix.Handler;
import ru.saidgadjiev.bibliography.bussiness.fix.PendingHandler;
import ru.saidgadjiev.bibliography.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliography.data.FilterArgumentResolver;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterCriteriaVisitor;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.domain.CompleteResult;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyFixSuggestRequest;
import ru.saidgadjiev.bibliography.model.CompleteRequest;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by said on 15.12.2018.
 */
@Service
public class BiographyFixService {

    private final BiographyFixDao biographyFixDao;

    private final SecurityService securityService;

    private final BiographyCategoryBiographyService biographyCategoryBiographyService;

    private final FilterArgumentResolver argumentResolver;

    private Map<BiographyFix.FixStatus, Handler> handlerMap = new HashMap<>();

    @Autowired
    public BiographyFixService(BiographyFixDao biographyFixDao,
                               SecurityService securityService,
                               BiographyCategoryBiographyService biographyCategoryBiographyService,
                               FilterArgumentResolver argumentResolver) {
        this.biographyFixDao = biographyFixDao;
        this.securityService = securityService;
        this.biographyCategoryBiographyService = biographyCategoryBiographyService;
        this.argumentResolver = argumentResolver;

        initHandlers();
    }

    public Page<BiographyFix> getFixesList(OffsetLimitPageRequest pageRequest, String query) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node parsed = new RSQLParser(new HashSet<ComparisonOperator>() {{
                add(RSQLOperators.EQUAL);
            }}).parse(query);

            parsed.accept(new FilterCriteriaVisitor<>(criteria, new HashMap<String, FilterCriteriaVisitor.Type>() {{
                put("fixer_id", FilterCriteriaVisitor.Type.INTEGER);
                put("status", FilterCriteriaVisitor.Type.INTEGER);
            }}));
        }

        List<BiographyFix> biographyFixes = biographyFixDao.getFixesList(
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                criteria,
                pageRequest.getSort()
        );

        if (biographyFixes.isEmpty()) {
            return new PageImpl<>(biographyFixes, pageRequest, 0);
        }
        long total = biographyFixDao.countOff();
        Collection<Integer> ids = biographyFixes.stream().map(BiographyFix::getBiographyId).collect(Collectors.toList());

        Map<Integer, Collection<String>> categories = biographyCategoryBiographyService.getBiographiesCategories(ids);

        for (BiographyFix fix: biographyFixes) {
            fix.getBiography().setCategories(categories.get(fix.getBiographyId()));
        }

        return new PageImpl<>(biographyFixes, pageRequest, total);
    }

    public void suggest(int biographyId, BiographyFixSuggestRequest suggestRequest) {
        User details = (User) securityService.findLoggedInUser();
        BiographyFix biographyFix = new BiographyFix();

        biographyFix.setFixText(suggestRequest.getFixText());
        biographyFix.setCreatorId(details.getId());
        biographyFix.setBiographyId(biographyId);

        biographyFixDao.create(biographyFix);
    }

    public CompleteResult<BiographyFix, FixAction> complete(int fixId, CompleteRequest completeRequest) throws SQLException {
        BiographyFix fix = doComplete(fixId, completeRequest);

        return new CompleteResult<>(fix == null ? 0 : 1, fix, getActions(fix));
    }

    public Collection<FixAction> getActions(BiographyFix fix) {
        return handlerMap.get(fix.getStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("fixerName", fix.getFixerId());
                    put("status", fix.getStatus());
                }}
        );
    }

    public BiographyFix getFixerInfo(int fixId) {
        return biographyFixDao.getFixerInfo(fixId);
    }

    private void initHandlers() {
        handlerMap.put(BiographyFix.FixStatus.CLOSED, new ClosedHandler());
        handlerMap.put(BiographyFix.FixStatus.PENDING, new PendingHandler(biographyFixDao));
    }

    private BiographyFix doComplete(int fixId, CompleteRequest completeRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Map<String, Object> processValues = new HashMap<>();

        processValues.put("fixId", fixId);
        processValues.put("fixerName", userDetails.getUsername());

        Handler handler = handlerMap.get(
                BiographyFix.FixStatus.fromCode(completeRequest.getStatus())
        );

        BiographyFix updated = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (updated == null) {
            return null;
        }

        if (updated.getFixerId() != null) {
            updated.setFixerBiography(userDetails.getBiography());
        }

        return updated;
    }
}
