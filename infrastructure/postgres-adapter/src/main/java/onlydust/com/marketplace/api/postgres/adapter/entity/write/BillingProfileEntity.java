package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.api.postgres.adapter.entity.GlobalSettingsEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileStatsViewEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
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

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KycEntity kyc;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KybEntity kyb;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    BankAccountEntity bankAccount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    Set<WalletEntity> wallets;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    PayoutInfoEntity payoutInfo;
    Boolean enabled;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "billing_profile_id", insertable = false, updatable = false)
    BillingProfileStatsViewEntity stats;

    @ManyToOne
    @JoinFormula(value = "1")
    GlobalSettingsEntity globalSettings;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public static BillingProfileEntity fromDomain(final BillingProfile billingProfile, final UserId ownerId, final ZonedDateTime ownerJoinedAt) {
        return BillingProfileEntity.builder()
                .id(billingProfile.id().value())
                .name(billingProfile.name())
                .verificationStatus(billingProfile.status())
                .type(billingProfile.type())
                .users(isNull(ownerId) ? Set.of() : Set.of(BillingProfileUserEntity.builder()
                        .billingProfileId(billingProfile.id().value())
                        .userId(ownerId.value())
                        .role(BillingProfile.User.Role.ADMIN)
                        .joinedAt(Date.from(ownerJoinedAt.toInstant()))
                        .build())
                )
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
                    .members(users.stream().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role)).collect(toSet()))
                    .invoiceMandateAcceptedAt(invoiceMandateAcceptedAt)
                    .invoiceMandateLatestVersionDate(globalSettings.getInvoiceMandateLatestVersionDate())
                    .build();
            case SELF_EMPLOYED -> SelfEmployedBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyb(kyb.toDomain())
                    .owner(users.stream().findFirst().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role)).orElseThrow())
                    .invoiceMandateAcceptedAt(invoiceMandateAcceptedAt)
                    .invoiceMandateLatestVersionDate(globalSettings.getInvoiceMandateLatestVersionDate())
                    .build();
            case INDIVIDUAL -> IndividualBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyc(kyc.toDomain())
                    .owner(users.stream().findFirst().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role)).orElseThrow())
                    .invoiceMandateAcceptedAt(invoiceMandateAcceptedAt)
                    .invoiceMandateLatestVersionDate(globalSettings.getInvoiceMandateLatestVersionDate())
                    .build();
        };
    }

}
