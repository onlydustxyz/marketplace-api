package onlydust.com.marketplace.api.read.entities.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubUserLinkJson {
    @NonNull
    Long githubUserId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;
}