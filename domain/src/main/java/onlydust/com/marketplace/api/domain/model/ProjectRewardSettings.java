package onlydust.com.marketplace.api.domain.model;

import java.time.ZoneId;
import java.util.Date;
import lombok.Value;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@Value
public class ProjectRewardSettings {

  Boolean ignorePullRequests;
  Boolean ignoreIssues;
  Boolean ignoreCodeReviews;
  Date ignoreContributionsBefore;

  public ProjectRewardSettings(boolean ignorePullRequests, boolean ignoreIssues, boolean ignoreCodeReviews,
      Date ignoreContributionsBefore) {
    int ignoredTypes = (ignorePullRequests ? 1 : 0) + (ignoreIssues ? 1 : 0) + (ignoreCodeReviews ? 1 : 0);
    if (ignoredTypes == 3) {
      throw OnlyDustException.badRequest("At least one type of contribution should be not ignored");
    }
    this.ignorePullRequests = ignorePullRequests;
    this.ignoreIssues = ignoreIssues;
    this.ignoreCodeReviews = ignoreCodeReviews;
    this.ignoreContributionsBefore = ignoreContributionsBefore;
  }


  public static ProjectRewardSettings defaultSettings(Date now) {
    return new ProjectRewardSettings(false, false, false,
        Date.from(now.toInstant().atZone(ZoneId.of("UTC")).minusMonths(1).toInstant()));
  }
}
