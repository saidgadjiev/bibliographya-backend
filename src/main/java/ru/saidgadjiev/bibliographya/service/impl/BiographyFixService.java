package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.bussiness.fix.EmptyHandler;
import ru.saidgadjiev.bibliographya.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliographya.bussiness.fix.Handler;
import ru.saidgadjiev.bibliographya.bussiness.fix.PendingHandler;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterCriteriaVisitor;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.domain.BiographyLike;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.builder.BiographyBuilder;
import ru.saidgadjiev.bibliographya.model.BiographyFixSuggestRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import javax.script.ScriptException;
import java.sql.PreparedStatement;
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

    private BiographyBuilder biographyBuilder;

    private Map<BiographyFix.FixStatus, Handler> handlerMap = new HashMap<>();

    @Autowired
    public BiographyFixService(BiographyFixDao biographyFixDao,
                               SecurityService securityService,
                               BiographyBuilder biographyBuilder) {
        this.biographyFixDao = biographyFixDao;
        this.securityService = securityService;
        this.biographyBuilder = biographyBuilder;

        initHandlers();
    }

    public Page<BiographyFix> getFixesList(OffsetLimitPageRequest pageRequest,
                                           String query,
                                           Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
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
        User user = (User) securityService.findLoggedInUser();

        Collection<FilterCriteria> isLikedCriteria = new ArrayList<>();

        isLikedCriteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(BiographyLike.USER_ID)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(user.getId())
                        .valueSetter(PreparedStatement::setInt)
                        .needPreparedSet(true)
                        .build()
        );

        List<BiographyFix> biographyFixes = biographyFixDao.getFixesList(
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                criteria,
                isLikedCriteria,
                pageRequest.getSort()
        );

        if (biographyFixes.isEmpty()) {
            return new PageImpl<>(biographyFixes, pageRequest, 0);
        }
        long total = biographyFixDao.countOff();

        biographyBuilder.builder(biographyFixes.stream().map(BiographyFix::getBiography).collect(Collectors.toList()))
                .buildCategories()
                .truncateBiography(biographyClampSize);

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

    public CompleteResult<BiographyFix> complete(int fixId, CompleteRequest completeRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Map<String, Object> processValues = new HashMap<>();

        processValues.put("fixId", fixId);
        processValues.put("fixerId", userDetails.getId());
        processValues.put("info", completeRequest.getInfo());

        Handler handler = handlerMap.get(
                BiographyFix.FixStatus.fromCode(completeRequest.getStatus())
        );

        BiographyFix fix = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (fix == null) {
            return new CompleteResult<>(1, null);
        }

        if (fix.getFixerId() != null) {
            fix.setFixer(userDetails.getBiography());
        }

        return new CompleteResult<>(1, fix);
    }

    public Collection<FixAction> getActions(BiographyFix fix) {
        User user = (User) securityService.findLoggedInUser();

        return handlerMap.get(fix.getStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("fixerId", fix.getFixerId());
                    put("status", fix.getStatus());
                    put("user", user);
                }}
        );
    }

    public BiographyFix getFixerInfo(int fixId) {
        return biographyFixDao.getFixerInfo(fixId);
    }

    private void initHandlers() {
        handlerMap.put(BiographyFix.FixStatus.CLOSED, new EmptyHandler());
        handlerMap.put(BiographyFix.FixStatus.PENDING, new PendingHandler(biographyFixDao));
        handlerMap.put(BiographyFix.FixStatus.IGNORED, new EmptyHandler());
    }
}
