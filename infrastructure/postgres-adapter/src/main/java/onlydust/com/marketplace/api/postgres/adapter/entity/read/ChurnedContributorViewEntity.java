package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import static java.util.Objects.nonNull;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.view.ChurnedContributorView;
import onlydust.com.marketplace.api.domain.view.ShortRepoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "profile_cover", typeClass = PostgreSQLEnumType.class)
public class ChurnedContributorViewEntity {

  @Id
  Long id;
  String login;
  String htmlUrl;
  String avatarUrl;
  Boolean isRegistered;
  @Enumerated(EnumType.STRING)
  @Type(type = "profile_cover")
  ProfileCoverEnumEntity cover;
  String lastContributionId;
  ZonedDateTime lastContributionCompletedAt;
  Long lastContributedRepoId;
  String lastContributedRepoOwner;
  String lastContributedRepoName;
  String lastContributedRepoHtmlUrl;
  String lastContributedRepoDescription;

  public ChurnedContributorView toDomain() {
    return ChurnedContributorView.builder()
        .githubId(id)
        .login(login)
        .htmlUrl(htmlUrl)
        .avatarUrl(avatarUrl)
        .isRegistered(isRegistered)
        .cover(nonNull(cover) ? cover.toDomain() : null)
        .lastContribution(ChurnedContributorView.Contribution.builder()
            .id(lastContributionId)
            .completedAt(lastContributionCompletedAt)
            .repo(ShortRepoView.builder()
                .id(lastContributedRepoId)
                .owner(lastContributedRepoOwner)
                .name(lastContributedRepoName)
                .htmlUrl(lastContributedRepoHtmlUrl)
                .description(lastContributedRepoDescription)
                .build())
            .build())
        .build();
  }
}
