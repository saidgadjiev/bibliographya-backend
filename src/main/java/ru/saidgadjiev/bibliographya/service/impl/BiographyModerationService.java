package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.bussiness.moderation.*;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterCriteriaVisitor;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.sql.PreparedStatement;
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

    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest, String query) {
        Collection<FilterCriteria> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node parsed = new RSQLParser(new HashSet<ComparisonOperator>() {{
                add(RSQLOperators.EQUAL);
            }}).parse(query);

            parsed.accept(new FilterCriteriaVisitor<>(criteria, new HashMap<String, FilterCriteriaVisitor.Type>() {{
                put("moderator_id", FilterCriteriaVisitor.Type.INTEGER);
                put("moderation_status", FilterCriteriaVisitor.Type.INTEGER);
            }}));
        }
        criteria.add(
                new FilterCriteria.Builder<Boolean>()
                        .propertyName("is_autobiography")
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setBoolean)
                        .filterValue(false)
                        .needPreparedSet(true)
                        .build()
        );

        return biographyService.getBiographies(pageRequest, criteria, null);
    }

    public CompleteResult<Biography, ModerationAction> complete(int biographyId, CompleteRequest completeRequest) throws SQLException {
        Biography updated = doComplete(biographyId, completeRequest);

        return new CompleteResult<>(1, updated, getActions(updated));
    }

    public CompleteResult<Biography, ModerationAction> userComplete(int biographyId, CompleteRequest completeRequest) throws SQLException {
        Biography updated = doComplete(biographyId, completeRequest);

        return new CompleteResult<>(1, updated, getUserActions(updated));
    }

    private Biography doComplete(int biographyId, CompleteRequest completeRequest) throws SQLException {
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
            return null;
        }

        if (updated.getModeratorId() != null) {
            updated.setModeratorBiography(userDetails.getBiography());
        }

        return updated;
    }

    public Collection<ModerationAction> getActions(Biography biography) {
        User user = (User) securityService.findLoggedInUser();

        return handlerMap.get(biography.getModerationStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("user", user);
                    put("moderatorId", biography.getModeratorId());
                    put("moderationStatus", biography.getModerationStatus());
                }}
        );
    }

    public Collection<ModerationAction> getUserActions(Biography biography) {
        return handlerMap.get(biography.getModerationStatus()).getUserActions(Collections.emptyMap());
    }

    public Biography getModeratorInfo(int biographyId) {
        return biographyModerationDao.getModeratorInfo(biographyId);
    }
}
