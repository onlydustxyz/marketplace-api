package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.view.ChurnedContributorView;
import onlydust.com.marketplace.project.domain.view.ShortRepoView;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;

@EqualsAndHashCode
@Data
@Entity
@Immutable
public class ChurnedContributorViewEntity {
    @Id
    Long id;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
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
