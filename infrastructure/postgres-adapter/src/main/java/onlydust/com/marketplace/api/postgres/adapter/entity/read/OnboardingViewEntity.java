package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "onboardings", schema = "public")
public class OnboardingViewEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    UUID id;
    @Column(name = "terms_and_conditions_acceptance_date")
    Date termsAndConditionsAcceptanceDate;
    @Column(name = "profile_wizard_display_date")
    Date profileWizardDisplayDate;
}