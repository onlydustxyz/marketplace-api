package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "onboardings", schema = "public")
@Immutable
public class OnboardingViewEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    UUID id;
    @Column(name = "terms_and_conditions_acceptance_date")
    Date termsAndConditionsAcceptanceDate;
    @Column(name = "profile_wizard_display_date")
    Date profileWizardDisplayDate;
}
