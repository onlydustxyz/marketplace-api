package onlydust.com.marketplace.bff.read.entities.user;

import lombok.Data;
import lombok.NonNull;

@Data
public class GithubUserLinkJson {
    @NonNull
    Long githubUserId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;
}