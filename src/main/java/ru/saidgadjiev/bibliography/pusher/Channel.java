package ru.saidgadjiev.bibliography.pusher;

/**
 * Created by said on 04.01.2019.
 */
public enum Channel {

    BIOGRAPHY {
        @Override
        public String getName(Integer biographyId) {
            return "biography-" + biographyId;
        }
    };

    public abstract String getName(Integer biographyId);
}
