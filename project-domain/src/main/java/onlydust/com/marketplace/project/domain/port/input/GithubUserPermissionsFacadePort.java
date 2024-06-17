package onlydust.com.marketplace.project.domain.port.input;

public interface GithubUserPermissionsFacadePort {
    boolean isUserAuthorizedToApplyOnProject(Long githubUserId);
}
