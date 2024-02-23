package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "payout_preferences", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
public class PayoutPreferenceEntity {

    @EmbeddedId
    PrimaryKey id;
    UUID billingProfileId;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    @Embeddable
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID userId;
    }

    public static PayoutPreferenceEntity fromDomain(final ProjectId projectId, final BillingProfile.Id billingProfileId, final UserId userId) {
        return PayoutPreferenceEntity.builder()
                .id(PrimaryKey.builder()
                        .projectId(projectId.value())
                        .userId(userId.value())
                        .build())
                .billingProfileId(billingProfileId.value())
                .build();
    }
}
