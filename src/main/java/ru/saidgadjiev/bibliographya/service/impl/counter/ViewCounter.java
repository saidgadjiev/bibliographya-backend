package ru.saidgadjiev.bibliographya.service.impl.counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ViewCounter {

    private long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

    private ConcurrentHashMap<Integer, AtomicLong> viewCount = new ConcurrentHashMap<>();

    private PersistViewCountTask persistViewCountTask;

    private final int MAX_SIZE = 100;

    @Autowired
    public ViewCounter(PersistViewCountTask persistViewCountTask) {
        this.persistViewCountTask = persistViewCountTask;
    }

    public void hit(int biographyId) {
        if (viewCount.size() >= MAX_SIZE || TimeUtils.isExpired(expiredAt)) {
            synchronized (this) {
                if (viewCount.size() >= MAX_SIZE || TimeUtils.isExpired(expiredAt)) {
                    doPersist();
                }
            }
        }

        viewCount.putIfAbsent(biographyId, new AtomicLong());
        viewCount.get(biographyId).incrementAndGet();
    }

    private void doPersist() {
        Map<Integer, Long> tmp = viewCount.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

        persistViewCountTask.persistViewCount(tmp);
        viewCount.clear();
        expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
    }
}
