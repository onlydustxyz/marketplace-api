package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hibernate.annotations.Immutable;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Immutable
public class TechnologyQueryEntity {
    @Id
    String technology;
}
