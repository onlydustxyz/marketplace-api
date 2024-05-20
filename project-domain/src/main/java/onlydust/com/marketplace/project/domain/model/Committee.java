package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Committee {

    @NonNull
    Id id;
    @NonNull
    String name;
    @NonNull
    ZonedDateTime startDate;
    @NonNull
    ZonedDateTime endDate;
    @NonNull
    Status status;
    @NonNull
    List<ProjectQuestion> projectQuestions = new ArrayList<>();
    UUID sponsorId;

    public Committee(@NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.id = Id.random();
        this.status = Status.DRAFT;
        this.sponsorId = null;
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Committee.Id of(@NonNull final UUID uuid) {
            return Committee.Id.builder().uuid(uuid).build();
        }

        public static Committee.Id of(@NonNull final String uuid) {
            return Committee.Id.of(UUID.fromString(uuid));
        }
    }


    public enum Status {
        DRAFT, OPEN_TO_APPLICATIONS, OPEN_TO_VOTES, CLOSED
    }


    public record ProjectAnswer(@NonNull ProjectQuestion.Id projectQuestionId, String answer) {
    }

    public record Application(@NonNull UUID userId, @NonNull UUID projectId, @NonNull List<ProjectAnswer> answers) {
    }
}
