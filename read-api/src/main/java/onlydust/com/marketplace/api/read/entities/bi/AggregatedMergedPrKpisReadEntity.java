package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.Optional;

@Entity
@NoArgsConstructor(force = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class AggregatedMergedPrKpisReadEntity {
    @Id
    @NonNull
    @Getter
    ZonedDateTime timestamp;

    Integer mergedPrCount;

    Integer mergedPrCount() {
        return Optional.ofNullable(mergedPrCount).orElse(0);
    }
}
