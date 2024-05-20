package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.UUID;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ProjectQuestion {

    @NonNull
    Id id;
    @NonNull
    String question;
    @NonNull
    Boolean required;

    public ProjectQuestion(@NonNull String question, @NonNull Boolean required) {
        this.id = Id.random();
        this.question = question;
        this.required = required;
    }

    public ProjectQuestion(@NonNull Id id, @NonNull String question, @NonNull Boolean required) {
        this.id = id;
        this.question = question;
        this.required = required;
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static ProjectQuestion.Id of(@NonNull final UUID uuid) {
            return ProjectQuestion.Id.builder().uuid(uuid).build();
        }

        public static ProjectQuestion.Id of(@NonNull final String uuid) {
            return ProjectQuestion.Id.of(UUID.fromString(uuid));
        }
    }
}
