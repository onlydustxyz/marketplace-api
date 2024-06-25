package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Immutable
public class TechnologyQueryEntity {
    @Id
    String technology;
}
