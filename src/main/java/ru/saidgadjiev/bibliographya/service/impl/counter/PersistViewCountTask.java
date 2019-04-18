package ru.saidgadjiev.bibliographya.service.impl.counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.dao.impl.ViewCountDao;

import java.util.Map;

@Component
public class PersistViewCountTask {

    private ViewCountDao viewCountDao;

    @Autowired
    public PersistViewCountTask(ViewCountDao viewCountDao) {
        this.viewCountDao = viewCountDao;
    }

    @Async
    public void persistViewCount(Map<Integer, Long> viewCounts) {
        viewCounts.forEach((integer, aLong) -> viewCountDao.createOrUpdate(integer, aLong));
    }
}
