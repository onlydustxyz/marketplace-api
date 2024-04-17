package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import onlydust.com.marketplace.project.domain.view.ChurnedContributorView;
import onlydust.com.marketplace.project.domain.view.ShortRepoView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;

import static java.util.Objects.nonNull;

@EqualsAndHashCode
@Data
@Entity
public class ChurnedContributorViewEntity {
    @Id
    Long id;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "profile_cover")
    @JdbcType(PostgreSQLEnumJdbcType.class)
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
