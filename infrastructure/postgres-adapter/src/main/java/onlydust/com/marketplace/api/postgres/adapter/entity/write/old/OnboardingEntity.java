package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "onboardings", schema = "public")
public class OnboardingEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    UUID id;
    @Column(name = "terms_and_conditions_acceptance_date")
    Date termsAndConditionsAcceptanceDate;
    @Column(name = "profile_wizard_display_date")
    Date profileWizardDisplayDate;
}
