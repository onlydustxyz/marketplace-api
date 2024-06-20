package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "onboardings", schema = "public")
@Immutable
public class OnboardingViewEntity {
    @Id
    UUID userId;
    ZonedDateTime termsAndConditionsAcceptanceDate;
    ZonedDateTime profileWizardDisplayDate;
}
