package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Immutable
@Getter
@Accessors(fluent = true)
public class BillingProfileUserRightsQueryEntity {
    @Id
    UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "billing_profile_role")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    BillingProfile.User.Role userRole;
    Long billingProfileProcessingRewardsCount;
    Long userProcessingRewardsCount;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "billing_profile_role")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    BillingProfile.User.Role invitedRole;
    ZonedDateTime invitedAt;
    Long invitedByGithubUserId;
    String invitedByGithubLogin;
    String invitedByGithubAvatarUrl;
    Long billingProfileCoworkersCount;


    public BillingProfileUserRightsView toDomain() {
        return BillingProfileUserRightsView.builder()
                .billingProfileProcessingRewardsCount(this.billingProfileProcessingRewardsCount)
                .userProcessingRewardsCount(this.userProcessingRewardsCount)
                .role(this.userRole)
                .invitation(Objects.isNull(this.invitedByGithubUserId) ? null : BillingProfileUserRightsView.InvitationView.builder()
                        .githubUserId(GithubUserId.of(this.invitedByGithubUserId))
                        .githubAvatarUrl(this.invitedByGithubAvatarUrl)
                        .githubLogin(this.invitedByGithubLogin)
                        .invitedAt(this.invitedAt)
                        .role(this.invitedRole)
                        .build())
                .billingProfileCoworkersCount(this.billingProfileCoworkersCount)
                .build();
    }
}
