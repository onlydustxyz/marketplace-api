package onlydust.com.marketplace.api.domain.view;

import java.util.UUID;

public interface UserLinkView {
    UUID getId();

    Integer getGithubUserId();

    String getLogin();

    String getAvatarUrl();

    String getUrl();
}
