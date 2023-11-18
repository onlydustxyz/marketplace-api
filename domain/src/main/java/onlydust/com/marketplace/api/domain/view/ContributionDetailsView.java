package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.*;

import java.util.Date;
import java.util.List;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class ContributionDetailsView extends ContributionBaseView {
    String id;
    Date createdAt;
    Date completedAt;
    ContributionType type;
    ContributionStatus status;
    GithubUserIdentity contributor;
    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    Integer githubCommentsCount;
    UserLinkView githubAuthor;
    GithubRepo githubRepo;
    Project project;
    List<ContributionLinkView> links;
    List<ContributionRewardView> rewards;
    List<CodeReviewState> codeReviewStates;

    public ContributionDetailsView withRewards(List<ContributionRewardView> rewards) {
        return this.toBuilder()
                .rewards(rewards)
                .build();
    }
}
