package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
@IdClass(value = PayoutPreferenceViewEntity.PrimaryKey.class)
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
    @org.hibernate.annotations.Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    BillingProfileEntity.Type billingProfileType;
    Boolean billingProfileEnabled;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID projectId;
    }


    public PayoutPreferenceView toDomain() {
        return PayoutPreferenceView.builder()
                .shortBillingProfileView(isNull(billingProfileId) ? null :
                        ShortBillingProfileView.builder()
                                .id(BillingProfile.Id.of(this.billingProfileId))
                                .name(this.billingProfileName)
                                .type(this.billingProfileType.toDomain())
                                .enabled(this.billingProfileEnabled)
                                .build()
                )
                .shortProjectView(
                        ShortProjectView.builder()
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
