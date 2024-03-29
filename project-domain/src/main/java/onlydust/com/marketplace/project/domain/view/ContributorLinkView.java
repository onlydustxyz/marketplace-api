package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContributorLinkView implements UserLinkView {
    Long githubUserId;
    String login;
    String avatarUrl;
    Boolean isRegistered;
}
