package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

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
public class OnboardingEntity {
    @Id
    UUID userId;
    Date termsAndConditionsAcceptanceDate;
    Date completionDate;
}
