package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.UpdateBiographyRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by said on 22.10.2018.
 */
@Service
public class BiographyService {

    private final BiographyDao biographyDao;

    private final SecurityService securityService;

    @Autowired
    public BiographyService(BiographyDao biographyDao, SecurityService securityService) {
        this.biographyDao = biographyDao;
        this.securityService = securityService;
    }

    public void create(BiographyRequest biographyRequest) {
        UserDetails userDetails = securityService.findLoggedInUser();

        Biography biography = new Biography.Builder(
                biographyRequest.getFirstName(),
                biographyRequest.getLastName(),
                biographyRequest.getMiddleName()
        )
                .setCreatorName(userDetails.getUsername())
                .setUserName(biographyRequest.getUserName())
                .build();

        biographyDao.save(biography);
    }

    public Biography getBiographyByUsername(String username) {
        return biographyDao.getByUsername(username);
    }

    public Biography getBiographyById(String id) throws SQLException {
        return biographyDao.getById(id);
    }

    public Page<Biography> getBiographies(Pageable pageRequest) {
        List<Biography> biographies = biographyDao.getBiographiesList(pageRequest.getPageSize(), pageRequest.getOffset());
        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }

    public int update(Integer id, UpdateBiographyRequest updateBiographyRequest) {
        Biography.Builder builder = new Biography.Builder(
                updateBiographyRequest.getFirstName(),
                updateBiographyRequest.getLastName(),
                updateBiographyRequest.getMiddleName()
        );

        builder.setId(id);
        builder.setBiography(updateBiographyRequest.getBiography());

        return biographyDao.update(builder.build());
    }
}
