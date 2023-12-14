package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.model.UserProfileCover;
import onlydust.com.marketplace.api.domain.view.ChurnedContributorView;
import onlydust.com.marketplace.api.domain.view.ShortRepoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.nonNull;

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
