package ru.saidgadjiev.bibliographya.service.impl.counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyViewCountDao;

import java.util.Map;

@Component
public class PersistViewCountTask {

    private BiographyViewCountDao biographyViewCountDao;

    @Autowired
    public PersistViewCountTask(BiographyViewCountDao biographyViewCountDao) {
        this.biographyViewCountDao = biographyViewCountDao;
    }

    @Async
    public void persistViewCount(Map<Integer, Long> viewCounts) {
        viewCounts.forEach((integer, aLong) -> biographyViewCountDao.createOrUpdate(integer, aLong));
    }
}
