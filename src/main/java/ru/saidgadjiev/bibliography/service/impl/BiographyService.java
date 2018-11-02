package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyDao;
import ru.saidgadjiev.bibliography.domain.Biography;

/**
 * Created by said on 22.10.2018.
 */
@Service
public class BiographyService {

    private final BiographyDao biographyDao;

    public BiographyService(BiographyDao biographyDao) {
        this.biographyDao = biographyDao;
    }

    public Biography getUsernameBiography(String username) {
        return biographyDao.getByUsername(username);
    }

    public Page<Biography> getBiographies(Pageable pageRequest) {
        return null;
    }
}
