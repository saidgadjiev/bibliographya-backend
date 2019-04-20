package ru.saidgadjiev.bibliographya.service.impl.counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ViewCounter {

    private ConcurrentHashMap<Integer, AtomicLong> viewCount = new ConcurrentHashMap<>();

    private PersistViewCountTask persistViewCountTask;

    private static final int MAX_SIZE = 100;

    @Autowired
    public ViewCounter(PersistViewCountTask persistViewCountTask) {
        this.persistViewCountTask = persistViewCountTask;
    }

    public void hit(int biographyId) {
        if (viewCount.size() >= MAX_SIZE) {
            doPersist();
        }

        viewCount.putIfAbsent(biographyId, new AtomicLong());
        viewCount.get(biographyId).incrementAndGet();
    }

    @Scheduled(fixedDelay = 1000)
    public void doPersist() {
        Map<Integer, Long> tmp = viewCount.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

        persistViewCountTask.persistViewCount(tmp);
        viewCount.clear();
    }
}
