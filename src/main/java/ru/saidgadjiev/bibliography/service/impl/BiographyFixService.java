package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyFixDao;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;

import java.util.List;

/**
 * Created by said on 15.12.2018.
 */
@Service
public class BiographyFixService {

    private final BiographyFixDao biographyFixDao;

    @Autowired
    public BiographyFixService(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public Page<BiographyFix> getFixesList(OffsetLimitPageRequest pageRequest) {
        List<BiographyFix> biographyFixes = biographyFixDao.getFixesList(pageRequest.getPageSize(), pageRequest.getOffset());
        long total = biographyFixDao.countOff();

        return new PageImpl<>(biographyFixes, pageRequest, total);
    }
}
