package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContributorLinkView implements UserLinkView {
    Integer githubUserId;
    String login;
    String avatarUrl;
    String url;
}
