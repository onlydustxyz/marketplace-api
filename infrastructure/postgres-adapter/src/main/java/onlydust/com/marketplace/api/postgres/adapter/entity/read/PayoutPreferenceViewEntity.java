package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@IdClass(value = PayoutPreferenceViewEntity.PrimaryKey.class)
@Immutable
public class PayoutPreferenceViewEntity {

    @Id
    UUID userId;
    @Id
    UUID projectId;
    String projectName;
    String projectLogoUrl;
    String projectKey;
    String projectShortDescription;
    UUID billingProfileId;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID projectId;
    }


    public PayoutPreferenceView toDomain(ShortBillingProfileView billingProfile) {
        return PayoutPreferenceView.builder()
                .billingProfileView(billingProfile)
                .shortProjectView(
                        ProjectShortView.builder()
                                .id(ProjectId.of(this.projectId))
                                .logoUrl(this.projectLogoUrl)
                                .name(this.projectName)
                                .shortDescription(this.projectShortDescription)
                                .slug(this.projectKey)
                                .build()
                )
                .build();
    }
}
