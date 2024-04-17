package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import org.hibernate.annotations.Type;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
public class BillingProfileUserRightsViewEntity {
    @Id
    UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "billing_profile_role")
    @Type(PostgreSQLEnumType.class)
    BillingProfileUserEntity.Role userRole;
    Long billingProfileProcessingRewardsCount;
    Long userProcessingRewardsCount;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "billing_profile_role")
    @Type(PostgreSQLEnumType.class)
    BillingProfileUserEntity.Role invitedRole;
    ZonedDateTime invitedAt;
    Long invitedByGithubUserId;
    String invitedByGithubLogin;
    String invitedByGithubAvatarUrl;
    Long billingProfileCoworkersCount;


    public BillingProfileUserRightsView toDomain() {
        return BillingProfileUserRightsView.builder()
                .billingProfileProcessingRewardsCount(this.billingProfileProcessingRewardsCount)
                .userProcessingRewardsCount(this.userProcessingRewardsCount)
                .role(isNull(this.userRole) ? null : this.userRole.toDomain())
                .invitation(Objects.isNull(this.invitedByGithubUserId) ? null : BillingProfileUserRightsView.InvitationView.builder()
                        .githubUserId(GithubUserId.of(this.invitedByGithubUserId))
                        .githubAvatarUrl(this.invitedByGithubAvatarUrl)
                        .githubLogin(this.invitedByGithubLogin)
                        .invitedAt(this.invitedAt)
                        .role(this.invitedRole.toDomain())
                        .build())
                .billingProfileCoworkersCount(this.billingProfileCoworkersCount)
                .build();
    }
}
