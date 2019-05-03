package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.bussiness.moderation.*;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.mapper.BiographyModerationFieldsMapper;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.IsNull;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import javax.script.ScriptException;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by said on 09.12.2018.
 */
@Service
public class BiographyModerationService {

    private final BiographyService biographyService;

    private final BiographyModerationDao biographyModerationDao;

    private final SecurityService securityService;

    private Map<Biography.ModerationStatus, Handler> handlerMap = new HashMap<>();

    @Autowired
    public BiographyModerationService(BiographyService biographyService,
                                      BiographyModerationDao biographyModerationDao,
                                      SecurityService securityService) {
        this.biographyService = biographyService;
        this.biographyModerationDao = biographyModerationDao;
        this.securityService = securityService;

        initHandlers();
    }

    private void initHandlers() {
        handlerMap.put(Biography.ModerationStatus.PENDING, new PendingHandler(biographyModerationDao));
        handlerMap.put(Biography.ModerationStatus.APPROVED, new ApprovedHandler(biographyModerationDao));
        handlerMap.put(Biography.ModerationStatus.REJECTED, new RejectedHandler(biographyModerationDao));
    }

    public Page<Biography> getBiographies(TimeZone timeZone,
                                          OffsetLimitPageRequest pageRequest,
                                          String query,
                                          Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
        AndCondition condition = new AndCondition();
        List<PreparedSetter> values = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node parsed = new RSQLParser(new HashSet<ComparisonOperator>() {{
                add(RSQLOperators.EQUAL);
            }}).parse(query);

            ClientQueryVisitor<AndCondition, Void> filterCriteriaVisitor = new ClientQueryVisitor<>(new BiographyModerationFieldsMapper());

            parsed.accept(filterCriteriaVisitor);

            condition = filterCriteriaVisitor.getCondition();
            values = filterCriteriaVisitor.getValues();
        }

        condition.add(new IsNull(new ColumnSpec(Biography.USER_ID)));

        return biographyService.getBiographies(timeZone, pageRequest, condition, values,null, biographyClampSize);
    }

    @Transactional
    public CompleteResult<Biography> complete(int biographyId, CompleteRequest completeRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Map<String, Object> processValues = new HashMap<>();

        processValues.put("biographyId", biographyId);
        processValues.put("moderatorId", userDetails.getId());
        processValues.put("rejectText", completeRequest.getInfo());

        Handler handler = handlerMap.get(
                Biography.ModerationStatus.fromCode(completeRequest.getStatus())
        );

        Biography updated = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (updated == null) {
            return new CompleteResult<>(0, null);
        }

        if (updated.getModeratorId() != null) {
            updated.setModerator(userDetails.getBiography());
        }

        return new CompleteResult<>(1, updated);
    }

    @Transactional
    public CompleteResult<Biography> userComplete(int biographyId, CompleteRequest completeRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Map<String, Object> processValues = new HashMap<>();

        processValues.put("biographyId", biographyId);
        processValues.put("creatorId", userDetails.getId());
        processValues.put("rejectText", completeRequest.getInfo());

        Handler handler = handlerMap.get(
                Biography.ModerationStatus.fromCode(completeRequest.getStatus())
        );

        Biography updated = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (updated == null) {
            return new CompleteResult<>(0, null);
        }

        return new CompleteResult<>(1, updated);
    }

    public Collection<ModerationAction> getActions(@NotNull Biography biography) {
        User user = (User) securityService.findLoggedInUser();

        return handlerMap.get(biography.getModerationStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("user", user);
                    put("moderatorId", biography.getModeratorId());
                    put("moderationStatus", biography.getModerationStatus());
                }}
        );
    }

    public Collection<ModerationAction> getUserActions(@NotNull Biography biography) {
        return handlerMap.get(biography.getModerationStatus()).getUserActions(Collections.emptyMap());
    }

    public Biography getModeratorInfo(int biographyId) {
        return biographyModerationDao.getModeratorInfo(biographyId);
    }
}
