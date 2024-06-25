package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "onboardings", schema = "public")
@Immutable
public class OnboardingReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID userId;

    @Getter(AccessLevel.NONE)
    ZonedDateTime termsAndConditionsAcceptanceDate;
    ZonedDateTime profileWizardDisplayDate;

    @Setter(AccessLevel.NONE)
    @Formula("""
            terms_and_conditions_acceptance_date is not null and
            terms_and_conditions_acceptance_date > (select gs.terms_and_conditions_latest_version_date from global_settings gs where gs.id = 1)
            """)
    boolean hasAcceptedTermsAndConditions;
}
