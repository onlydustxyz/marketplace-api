package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.view.ContributorActivityView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ContributorActivityViewEntity {

  @Id
  Long id;
  String login;
  String htmlUrl;
  String avatarUrl;
  Boolean isRegistered;
  Integer completedPullRequestCount;
  Integer completedIssueCount;
  Integer completedCodeReviewCount;
  @Type(type = "jsonb")
  List<UserProfileEntity.WeekCount> counts;

  public ContributorActivityView toDomain() {
    return ContributorActivityView.builder()
        .githubId(id)
        .login(login)
        .htmlUrl(htmlUrl)
        .avatarUrl(avatarUrl)
        .isRegistered(isRegistered)
        .completedPullRequestCount(completedPullRequestCount)
        .completedIssueCount(completedIssueCount)
        .completedCodeReviewCount(completedCodeReviewCount)
        .contributionStats(counts.stream().map(UserProfileEntity.WeekCount::toDomain).toList())
        .build();
  }
}
