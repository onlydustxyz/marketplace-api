package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.kernel.model.UserId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "billing_profiles", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
public class BillingProfileEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    String name;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_type")
    BillingProfile.Type type;
    ZonedDateTime invoiceMandateAcceptedAt;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "verification_status")
    VerificationStatus verificationStatus;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "billingProfileId")
    Set<BillingProfileUserEntity> users;

    @OneToOne(mappedBy = "billingProfile")
    KycEntity kyc;

    @OneToOne(mappedBy = "billingProfile")
    KybEntity kyb;

    @OneToOne(mappedBy = "billingProfile")
    BankAccountEntity bankAccount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    Set<WalletEntity> wallets;

    @OneToOne(mappedBy = "billingProfile")
    PayoutInfoEntity payoutInfo;
    Boolean enabled;

    @Formula("(SELECT bps.mandate_acceptance_outdated from accounting.billing_profile_stats bps where bps.billing_profile_id = id)")
    Boolean invoiceMandateAcceptanceOutdated;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    private Date updatedAt;

    public static BillingProfileEntity fromDomain(final IndividualBillingProfile billingProfile) {
        return BillingProfileEntity.builder()
                .id(billingProfile.id().value())
                .name(billingProfile.name())
                .verificationStatus(billingProfile.status())
                .type(billingProfile.type())
                .users(Set.of(BillingProfileUserEntity.fromDomain(billingProfile.id(), billingProfile.owner())))
                .enabled(billingProfile.enabled())
                .invoiceMandateAcceptedAt(billingProfile.invoiceMandateAcceptedAt())
                .build();
    }

    public static BillingProfileEntity fromDomain(final SelfEmployedBillingProfile billingProfile) {
        return BillingProfileEntity.builder()
                .id(billingProfile.id().value())
                .name(billingProfile.name())
                .verificationStatus(billingProfile.status())
                .type(billingProfile.type())
                .users(Set.of(BillingProfileUserEntity.fromDomain(billingProfile.id(), billingProfile.owner())))
                .enabled(billingProfile.enabled())
                .invoiceMandateAcceptedAt(billingProfile.invoiceMandateAcceptedAt())
                .build();
    }

    public static BillingProfileEntity fromDomain(final CompanyBillingProfile billingProfile) {
        return BillingProfileEntity.builder()
                .id(billingProfile.id().value())
                .name(billingProfile.name())
                .verificationStatus(billingProfile.status())
                .type(billingProfile.type())
                .users(billingProfile.members().stream().map(u -> BillingProfileUserEntity.fromDomain(billingProfile.id(), u)).collect(toSet()))
                .enabled(billingProfile.enabled())
                .invoiceMandateAcceptedAt(billingProfile.invoiceMandateAcceptedAt())
                .build();
    }

    public BillingProfile toDomain() {
        return switch (type) {
            case COMPANY -> CompanyBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyb(kyb.toDomain())
                    .members(users.stream().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role, u.joinedAt)).collect(toSet()))
                    .invoiceMandateAcceptedAt(invoiceMandateAcceptedAt)
                    .invoiceMandateAcceptanceOutdated(invoiceMandateAcceptanceOutdated)
                    .build();
            case SELF_EMPLOYED -> SelfEmployedBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyb(kyb.toDomain())
                    .owner(users.stream().findFirst().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role, u.joinedAt)).orElseThrow())
                    .invoiceMandateAcceptedAt(invoiceMandateAcceptedAt)
                    .invoiceMandateAcceptanceOutdated(invoiceMandateAcceptanceOutdated)
                    .build();
            case INDIVIDUAL -> IndividualBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyc(kyc.toDomain())
                    .owner(users.stream().findFirst().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role, u.joinedAt)).orElseThrow())
                    .invoiceMandateAcceptedAt(invoiceMandateAcceptedAt)
                    .invoiceMandateAcceptanceOutdated(invoiceMandateAcceptanceOutdated)
                    .build();
        };
    }

}
