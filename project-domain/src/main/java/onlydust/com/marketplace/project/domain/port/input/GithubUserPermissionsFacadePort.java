package onlydust.com.marketplace.project.domain.port.input;

public interface GithubUserPermissionsFacadePort {
    void logout(Long githubUserId);

    boolean isUserAuthorizedToApplyOnProject(Long githubUserId);
}
