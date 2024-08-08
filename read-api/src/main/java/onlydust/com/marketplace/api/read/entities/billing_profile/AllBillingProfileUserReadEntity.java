package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
@IdClass(AllBillingProfileUserReadEntity.PrimaryKey.class)
@Table(name = "all_billing_profile_users", schema = "accounting")
@Immutable
public class AllBillingProfileUserReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;

    @Id
    @EqualsAndHashCode.Include
    Long githubUserId;

    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    @NonNull
    BillingProfileReadEntity billingProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull
    AllUserReadEntity user;

    @Enumerated(EnumType.STRING)
    BillingProfileCoworkerRole role;

    boolean invitationAccepted;

    public ShortBillingProfileResponse toShortResponse() {
        return billingProfile.toShortResponse(githubUserId);
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        Long githubUserId;
    }
}
