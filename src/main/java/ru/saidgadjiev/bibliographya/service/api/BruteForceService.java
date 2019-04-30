package ru.saidgadjiev.bibliographya.service.api;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by said on 28/04/2019.
 */
public interface BruteForceService {
    void count(HttpServletRequest request, Type type);

    boolean isBlocked(HttpServletRequest request, Type type);

    enum Type {

        SIGN_UP(3),

        SEND_VERIFICATION_CODE(2);

        private int blockedCount;

        Type(int blockedCount) {
            this.blockedCount = blockedCount;
        }

        public int getBlockedCount() {
            return blockedCount;
        }
    }
}
