package onlydust.com.marketplace.project.domain.model.recommendation;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@Accessors(fluent = true)
public class MatchingQuestion<T> {
    Id id;
    String body;
    String description;
    boolean multipleChoice;
    List<MatchingAnswer<T>> answers;

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        @JsonCreator
        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }
}

