package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyDao;
import ru.saidgadjiev.bibliography.data.FilterArgumentResolver;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.PreparedSetter;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 22.10.2018.
 */
@Service
public class BiographyService {

    private final BiographyDao biographyDao;

    private final SecurityService securityService;

    private final FilterArgumentResolver argumentResolver;

    @Autowired
    public BiographyService(BiographyDao biographyDao, SecurityService securityService, FilterArgumentResolver argumentResolver) {
        this.biographyDao = biographyDao;
        this.securityService = securityService;
        this.argumentResolver = argumentResolver;
    }

    public Biography create(BiographyRequest biographyRequest) throws SQLException {
        UserDetails userDetails = securityService.findLoggedInUser();

        Biography biography = new Biography.Builder(
                biographyRequest.getFirstName(),
                biographyRequest.getLastName(),
                biographyRequest.getMiddleName()
        )
                .setBiography(biographyRequest.getBiography())
                .setCreatorName(userDetails.getUsername())
                .setUserName(biographyRequest.getUserName())
                .build();

        return biographyDao.save(biography);
    }

    public Biography getBiography(String userNameFilter) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (userNameFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            "user_name",
                            String::valueOf,
                            PreparedStatement::setString,
                            userNameFilter
                    )
            );
        }

        return biographyDao.getBiography(criteria);
    }

    public Biography getBiographyById(int id) {
        return biographyDao.getById(id);
    }

    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest,
                                          String creatorNameFilter,
                                          String moderationStatusFilter,
                                          String categoryName) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (creatorNameFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            "creator_name",
                            String::valueOf, PreparedStatement::setString,
                            creatorNameFilter
                    )
            );
        }
        if (moderationStatusFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            Biography.MODERATION_STATUS,
                            String::valueOf,
                            PreparedStatement::setString,
                            moderationStatusFilter
                    )
            );
        }

        List<Biography> biographies = biographyDao.getBiographiesList(
                pageRequest.getPageSize(), pageRequest.getOffset(), criteria, categoryName
        );
        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }

    public BiographyUpdateStatus update(Integer id, BiographyRequest updateBiographyRequest) throws SQLException {
        Biography.Builder builder = new Biography.Builder(
                updateBiographyRequest.getFirstName(),
                updateBiographyRequest.getLastName(),
                updateBiographyRequest.getMiddleName()
        );

        builder.setId(id);
        builder.setBiography(updateBiographyRequest.getBiography());

        Timestamp timestamp = new Timestamp(updateBiographyRequest.getLastModified().getTime());

        timestamp.setNanos(updateBiographyRequest.getLastModified().getNanos());

        builder.setUpdatedAt(timestamp);

        return biographyDao.update(builder.build());
    }
}
