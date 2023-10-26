package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class ProjectRewardSettings {
    Boolean ignorePullRequests;
    Boolean ignoreIssues;
    Boolean ignoreCodeReviews;
    Date ignoreContributionsBefore;

    public static ProjectRewardSettings defaultSettings() {
        return ProjectRewardSettings.builder()
                .ignorePullRequests(false)
                .ignoreIssues(false)
                .ignoreCodeReviews(false)
                .ignoreContributionsBefore(null)
                .build();
    }
}
