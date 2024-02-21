package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectLeaderLinkView implements UserLinkView {
    UUID id;
    Long githubUserId;
    String login;
    String avatarUrl;
    String url;
}
