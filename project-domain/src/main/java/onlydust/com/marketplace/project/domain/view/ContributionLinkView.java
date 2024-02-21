package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.model.GithubRepo;

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
