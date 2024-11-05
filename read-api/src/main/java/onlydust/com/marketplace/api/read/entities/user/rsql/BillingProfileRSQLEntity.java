package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.BillingProfileType;
import onlydust.com.backoffice.api.contract.model.UserSearchBillingProfile;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.api.read.entities.billing_profile.KybReadEntity;
import onlydust.com.marketplace.api.read.entities.billing_profile.KycReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Accessors(fluent = true, chain = true)
@Table(name = "billing_profiles", schema = "accounting")
@Immutable
public class BillingProfileRSQLEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String name;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "accounting.billing_profile_type")
    @NonNull
    BillingProfileType type;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "accounting.verification_status")
    @NonNull
    VerificationStatus verificationStatus;

    @OneToOne(mappedBy = "billingProfile")
    KycReadEntity kyc;

    @OneToOne(mappedBy = "billingProfile")
    KybReadEntity kyb;

    public UserSearchBillingProfile toUserSearch() {
        return new UserSearchBillingProfile()
                .id(id)
                .name(name)
                .type(type)
                .status(verificationStatus)
                .kyb(kyb == null ? null : kyb.toUserSearch())
                .kyc(kyc == null ? null : kyc.toUserSearch())
                ;
    }

}
