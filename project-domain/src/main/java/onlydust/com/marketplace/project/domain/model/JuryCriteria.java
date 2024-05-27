package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.UUID;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode
@Builder
public class JuryCriteria {

    @NonNull
    Id id;
    @NonNull
    String criteria;

    public JuryCriteria(@NonNull Id id, @NonNull String criteria) {
        this.id = id;
        this.criteria = criteria;
    }

    public JuryCriteria(@NonNull String criteria) {
        this.id = Id.random();
        this.criteria = criteria;
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static JuryCriteria.Id of(@NonNull final UUID uuid) {
            return JuryCriteria.Id.builder().uuid(uuid).build();
        }

        public static JuryCriteria.Id of(@NonNull final String uuid) {
            return JuryCriteria.Id.of(UUID.fromString(uuid));
        }
    }
}
