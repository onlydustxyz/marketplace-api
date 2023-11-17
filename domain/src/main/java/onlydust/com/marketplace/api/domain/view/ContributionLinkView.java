package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;

import java.util.List;

@Value
@Builder
@EqualsAndHashCode(callSuper = true)
public class ContributionLinkView extends ContributionBaseView {
    ContributionType type;
    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    UserLinkView githubAuthor;
    GithubRepo githubRepo;
    Boolean isMine;
    List<CodeReviewState> codeReviewStates;
}
