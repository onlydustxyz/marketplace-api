package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Data
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "billing_profile_role", typeClass = PostgreSQLEnumType.class)
public class ShortBillingProfileViewEntity {
    @Id
    UUID id;

    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    BillingProfileEntity.Type type;

    @Type(type = "billing_profile_role")
    @Enumerated(EnumType.STRING)
    BillingProfileUserEntity.Role role;

    @Type(type = "verification_status")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity verificationStatus;

    String name;
    Date invoiceMandateAcceptedAt;
    Boolean enabled;
    Boolean pendingInvitation;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "billingProfileId")
    BillingProfileStatsViewEntity stats;

    public ZonedDateTime getInvoiceMandateAcceptedAt() {
        return isNull(invoiceMandateAcceptedAt) ? null : new Date(invoiceMandateAcceptedAt.getTime()).toInstant().atZone(ZoneOffset.UTC);
    }

    public ShortBillingProfileView toView() {
        return ShortBillingProfileView.builder()
                .id(BillingProfile.Id.of(this.id))
                .type(this.type.toDomain())
                .role(this.role.toDomain())
                .verificationStatus(this.verificationStatus.toDomain())
                .name(this.name)
                .enabled(this.enabled)
                .pendingInvitationResponse(this.pendingInvitation)
                .invoiceMandateAcceptedAt(this.getInvoiceMandateAcceptedAt())
                .rewardCount(this.stats.rewardCount())
                .invoiceableRewardCount(this.stats.invoiceableRewardCount())
                .missingVerification(this.stats.missingVerification())
                .missingPayoutInfo(this.stats.missingPayoutInfo())
                .build();
    }
}
