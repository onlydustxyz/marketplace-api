package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Data;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Where;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Data
@Immutable
public class ShortBillingProfileQueryEntity {
    @Id
    UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_type")
    BillingProfile.Type type;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_role")
    BillingProfile.User.Role role;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "verification_status")
    VerificationStatus verificationStatus;

    String name;
    Date invoiceMandateAcceptedAt;
    Boolean enabled;
    Boolean pendingInvitation;
    Boolean individualLimitReached;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "billingProfileId")
    BillingProfileStatsViewEntity stats;

    @OneToMany
    @JoinColumn(name = "billingProfileId", referencedColumnName = "id")
    @Where(clause = """
            id in (
                select rsd.reward_id
                from accounting.reward_status_data rsd
                where date_trunc('month', rsd.paid_at)::date = date_trunc('month', CURRENT_DATE)::date
            )
            """)
    List<RewardViewEntity> currentMonthRewards;

    public ZonedDateTime getInvoiceMandateAcceptedAt() {
        return isNull(invoiceMandateAcceptedAt) ? null : new Date(invoiceMandateAcceptedAt.getTime()).toInstant().atZone(ZoneOffset.UTC);
    }

    public ShortBillingProfileView toView() {
        return ShortBillingProfileView.builder()
                .id(BillingProfile.Id.of(this.id))
                .type(this.type)
                .role(this.role)
                .verificationStatus(this.verificationStatus)
                .name(this.name)
                .enabled(this.enabled)
                .pendingInvitationResponse(this.pendingInvitation)
                .invoiceMandateAcceptedAt(this.getInvoiceMandateAcceptedAt())
                .rewardCount(this.stats.rewardCount())
                .invoiceableRewardCount(this.stats.invoiceableRewardCount())
                .missingVerification(this.stats.missingVerification())
                .missingPayoutInfo(this.stats.missingPayoutInfo())
                .individualLimitReached(this.individualLimitReached)
                .build();
    }
}
