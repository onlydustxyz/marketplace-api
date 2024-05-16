package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "billing_profiles", schema = "accounting")
@Immutable
public class BillingProfileViewEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    String name;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_type")
    BillingProfile.Type type;
    Date invoiceMandateAcceptedAt;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "verification_status")
    VerificationStatus verificationStatus;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "billingProfileId")
    Set<BillingProfileUserViewEntity> users;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "billingProfile")
    KycViewEntity kyc;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "billingProfile")
    KybViewEntity kyb;

    Boolean enabled;

    public BillingProfile toDomain() {
        return switch (type) {
            case COMPANY -> CompanyBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyb(kyb.toDomain())
                    .members(users.stream().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role)).collect(toSet()))
                    .build();
            case SELF_EMPLOYED -> SelfEmployedBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyb(kyb.toDomain())
                    .owner(users.stream().findFirst().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role)).orElseThrow())
                    .build();
            case INDIVIDUAL -> IndividualBillingProfile.builder()
                    .id(BillingProfile.Id.of(id))
                    .name(name)
                    .status(verificationStatus)
                    .enabled(enabled)
                    .kyc(kyc.toDomain())
                    .owner(users.stream().findFirst().map(u -> new BillingProfile.User(UserId.of(u.userId), u.role)).orElseThrow())
                    .build();
        };
    }

}
