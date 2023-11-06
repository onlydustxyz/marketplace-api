package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;

import java.util.Date;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class MyContributionDetailsView {
    String id;
    Date createdAt;
    Date completedAt;
    ContributionType type;
    ContributionStatus status;
    GithubUserIdentity contributor;
    Long githubNumber;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    String projectName;
    String repoName;
    List<ContributionLinkView> links;
    List<MyContributionRewardView> rewards;

    public MyContributionDetailsView withRewards(List<MyContributionRewardView> rewards) {
        return this.toBuilder()
                .rewards(rewards)
                .build();
    }
}
