package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

import static java.util.Objects.isNull;

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
    String billingProfileName;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "billing_profile_type")
    BillingProfileEntity.Type billingProfileType;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID projectId;
    }


    public PayoutPreferenceView toDomain() {
        return PayoutPreferenceView.builder()
                .billingProfileView(isNull(billingProfileId) ? null :
                        PayoutPreferenceView.BillingProfileView.builder()
                                .id(BillingProfile.Id.of(this.billingProfileId))
                                .name(this.billingProfileName)
                                .type(this.billingProfileType.toDomain())
                                .build()
                )
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
