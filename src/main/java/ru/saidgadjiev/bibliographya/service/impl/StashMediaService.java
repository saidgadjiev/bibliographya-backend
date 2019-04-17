package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.StashMediaDao;

@Service
public class StashMediaService {

    private StashMediaDao stashMediaDao;

    @Autowired
    public StashMediaService(StashMediaDao stashMediaDao) {
        this.stashMediaDao = stashMediaDao;
    }

    public void create(String path) {
        stashMediaDao.create(path);
    }

    public void remove(String path) {
        stashMediaDao.remove(path);
    }
}
