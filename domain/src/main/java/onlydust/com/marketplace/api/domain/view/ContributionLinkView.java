package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;

import java.util.List;

@Value
@Builder
@EqualsAndHashCode
public class ContributionLinkView {
    ContributionType type;
    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    UserLinkView githubAuthor;
    GithubRepo githubRepo;
    Boolean isMine;
}
