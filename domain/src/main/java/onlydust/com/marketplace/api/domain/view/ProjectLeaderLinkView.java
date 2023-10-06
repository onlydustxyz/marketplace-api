package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectLeaderLinkView implements UserLinkView {
    UUID id;
    Integer githubUserId;
    String login;
    String avatarUrl;
    String url;
}
