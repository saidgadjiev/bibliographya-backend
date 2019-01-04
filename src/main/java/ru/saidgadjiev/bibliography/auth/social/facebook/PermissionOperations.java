package ru.saidgadjiev.bibliography.auth.social.facebook;

/**
 * Created by said on 29.12.2018.
 */
public class PermissionOperations {

    private final GraphApi graphApi;

    public PermissionOperations(GraphApi graphApi) {
        this.graphApi = graphApi;
    }

    public void deletePermissions(String userId) {
        graphApi.delete(userId + "/permissions", null);
    }
}
