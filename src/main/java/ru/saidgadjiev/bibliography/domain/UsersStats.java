package ru.saidgadjiev.bibliography.domain;

import ru.saidgadjiev.bibliography.auth.common.ProviderType;

import java.util.Map;

public class UsersStats {

    private long count;

    private Map<ProviderType, Integer> usersByProvider;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Map<ProviderType, Integer> getUsersByProvider() {
        return usersByProvider;
    }

    public void setUsersByProvider(Map<ProviderType, Integer> usersByProvider) {
        this.usersByProvider = usersByProvider;
    }
}
