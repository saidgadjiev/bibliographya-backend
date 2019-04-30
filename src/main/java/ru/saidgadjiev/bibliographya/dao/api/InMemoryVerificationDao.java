package ru.saidgadjiev.bibliographya.dao.api;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Verification;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class InMemoryVerificationDao implements VerificationDao {

    private final Object mutex = new Object();

    private List<Verification> verifications = new ArrayList<>();

    @Override
    public void create(Verification verification) {
        synchronized (mutex) {
            verifications.add(verification);
        }
    }

    @Override
    public Verification get(String verificationKey, String code) {
        synchronized (mutex) {
            return verifications
                    .stream()
                    .filter(verification -> {
                        return verification.getVerificationKey().equals(verificationKey)
                                && verification.getCode().equals(code);
                    }).findAny()
                    .orElse(null);
        }
    }

    @Override
    public void remove(Verification verification) {
        synchronized (mutex) {
            for (Iterator<Verification> iterator = verifications.iterator(); iterator.hasNext();) {
                Verification next = iterator.next();

                if (next.equals(verification)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Scheduled(cron = "0 0/10 * * * *")
    public void expire() {
        synchronized (mutex) {
            verifications.removeIf(verification -> TimeUtils.isExpired(verification.getExipredAt()));
        }
    }
}
