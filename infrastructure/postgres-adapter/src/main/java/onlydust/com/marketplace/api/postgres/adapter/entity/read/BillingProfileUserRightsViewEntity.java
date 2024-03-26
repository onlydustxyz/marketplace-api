package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@TypeDef(name = "billing_profile_role", typeClass = PostgreSQLEnumType.class)
public class BillingProfileUserRightsViewEntity {
    @Id
    UUID userId;
    @Type(type = "billing_profile_role")
    @Enumerated(EnumType.STRING)
    BillingProfileUserEntity.Role userRole;
    @Column(name = "has_bp_some_invoices")
    Boolean hasBillingProfileSomeInvoices;
    Boolean hasUserSomeLinkedInvoices;
    @Type(type = "billing_profile_role")
    @Enumerated(EnumType.STRING)
    BillingProfileUserEntity.Role invitedRole;
    ZonedDateTime invitedAt;
    Long invitedByGithubUserId;
    String invitedByGithubLogin;
    String invitedByGithubAvatarUrl;
    Boolean hasMoreThanOneCoworker;


    public BillingProfileUserRightsView toDomain() {
        return BillingProfileUserRightsView.builder()
                .hasBillingProfileSomeInvoices(this.hasBillingProfileSomeInvoices)
                .hasUserSomeRewardsIncludedInInvoicesOnBillingProfile(this.hasUserSomeLinkedInvoices)
                .role(isNull(this.userRole) ? null : this.userRole.toDomain())
                .invitation(Objects.isNull(this.invitedByGithubUserId) ? null : BillingProfileUserRightsView.InvitationView.builder()
                        .githubUserId(GithubUserId.of(this.invitedByGithubUserId))
                        .githubAvatarUrl(this.invitedByGithubAvatarUrl)
                        .githubLogin(this.invitedByGithubLogin)
                        .invitedAt(this.invitedAt)
                        .role(this.invitedRole.toDomain())
                        .build())
                .hasMoreThanOneCoworkers(this.hasMoreThanOneCoworker)
                .build();
    }
}
