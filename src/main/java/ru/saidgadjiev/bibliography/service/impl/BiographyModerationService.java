package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyDao;
import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.model.ModerationStatus;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;

/**
 * Created by said on 09.12.2018.
 */
@Service
public class BiographyModerationService {

    private final BiographyService biographyService;

    private final BiographyModerationDao biographyModerationDao;

    private final SecurityService securityService;

    private final BiographyDao biographyDao;

    @Autowired
    public BiographyModerationService(BiographyService biographyService,
                                      BiographyModerationDao biographyModerationDao,
                                      SecurityService securityService,
                                      BiographyDao biographyDao) {
        this.biographyService = biographyService;
        this.biographyModerationDao = biographyModerationDao;
        this.securityService = securityService;
        this.biographyDao = biographyDao;
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

    public int approve(int biographyId) {
        UserDetails userDetails = securityService.findLoggedInUser();

        return biographyModerationDao.updateStatus(biographyId, userDetails.getUsername(), ModerationStatus.APPROVED.getCode());
    }

    public int reject(int biographyId) {
        UserDetails userDetails = securityService.findLoggedInUser();

        return biographyModerationDao.updateStatus(biographyId, userDetails.getUsername(), ModerationStatus.REJECTED.getCode());
    }

    public int assignMe(int biographyId) {
        UserDetails userDetails = securityService.findLoggedInUser();

        return biographyModerationDao.assignMe(biographyId, userDetails.getUsername());
    }

    public Biography getModeratorInfo(int biographyId) {
        return biographyModerationDao.getModeratorInfo(biographyId);
    }

    public int release(int biographyId) {
        UserDetails userDetails = securityService.findLoggedInUser();

        return biographyModerationDao.release(biographyId, userDetails.getUsername());
    }

    public int pending(int biographyId) {
        UserDetails userDetails = securityService.findLoggedInUser();

        return biographyModerationDao.updateStatus(biographyId, userDetails.getUsername(), ModerationStatus.PENDING.getCode());
    }
}
