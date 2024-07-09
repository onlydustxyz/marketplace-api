package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.PayoutPreferencesItemResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
@IdClass(value = PayoutPreferenceReadEntity.PrimaryKey.class)
@Table(name = "payout_preferences", schema = "accounting")
@Immutable
public class PayoutPreferenceReadEntity {

    @Id
    UUID userId;
    @Id
    UUID projectId;

    @ManyToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectReadEntity project;

    @ManyToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileReadEntity billingProfile;

    public PayoutPreferencesItemResponse toDto(Long callerGithubUserId) {
        return new PayoutPreferencesItemResponse()
                .project(project.toShortResponse())
                .billingProfile(billingProfile == null ? null : billingProfile.toShortResponse(callerGithubUserId));
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID projectId;
    }
}
