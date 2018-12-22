package ru.saidgadjiev.bibliography.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.bussiness.moderation.*;
import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.CompleteResult;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.CompleteRequest;
import ru.saidgadjiev.bibliography.model.ModerationStatus;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 09.12.2018.
 */
@Service
public class BiographyModerationService {

    private final BiographyService biographyService;

    private final BiographyModerationDao biographyModerationDao;

    private final SecurityService securityService;

    private Map<ModerationStatus, Handler> handlerMap = new HashMap<>();

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
        handlerMap.put(ModerationStatus.PENDING, new PendingHandler(biographyModerationDao));
        handlerMap.put(ModerationStatus.APPROVED, new ApprovedHandler(biographyModerationDao));
        handlerMap.put(ModerationStatus.REJECTED, new RejectedHandler(biographyModerationDao));
    }

    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest,
                                          String moderatorNameFilter,
                                          String moderationStatusFilter) {
        return biographyService.getBiographies(
                pageRequest,
                null,
                moderationStatusFilter,
                moderatorNameFilter,
                null
        );
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
        processValues.put("moderatorName", userDetails.getUsername());
        processValues.put("rejectText", completeRequest.getRejectText());

        Handler handler = handlerMap.get(
                ModerationStatus.fromCode(completeRequest.getStatus())
        );

        Biography updated = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (updated == null) {
            return null;
        }

        if (StringUtils.isNotBlank(updated.getModeratorName())) {
            updated.setModeratorBiography(userDetails.getBiography());
        }

        return updated;
    }

    public Collection<ModerationAction> getActions(Biography biography) {
        return handlerMap.get(biography.getModerationStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("moderatorName", biography.getModeratorName());
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
