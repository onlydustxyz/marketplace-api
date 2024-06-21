package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Value
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "billing_profiles_user_invitations", schema = "accounting")
@IdClass(BillingProfileUserInvitationReadEntity.PrimaryKey.class)
public class BillingProfileUserInvitationReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;
    @Id
    @EqualsAndHashCode.Include
    Long githubUserId;

    @ManyToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileReadEntity billingProfile;

    @Enumerated(EnumType.STRING)
    BillingProfileCoworkerRole role;

    Date invitedAt;
    UUID invitedBy;
    Boolean accepted;

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        Long githubUserId;
    }
}
