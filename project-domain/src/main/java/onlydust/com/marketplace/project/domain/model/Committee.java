package onlydust.com.marketplace.project.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.*;

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
    ZonedDateTime applicationStartDate;
    @NonNull
    ZonedDateTime applicationEndDate;
    @NonNull
    Status status;

    @Builder.Default
    @NonNull
    List<ProjectQuestion> projectQuestions = new ArrayList<>();

    @Builder.Default
    @NonNull
    Map<ProjectId, Application> projectApplications = new HashMap<>();

    @Builder.Default
    @NonNull
    List<UserId> juryIds = new ArrayList<>();

    @Builder.Default
    @NonNull
    List<JuryCriteria> juryCriteria = new ArrayList<>();

    Integer votePerJury;

    public static Committee create(@NonNull String name, @NonNull ZonedDateTime applicationStartDate, @NonNull ZonedDateTime applicationEndDate) {
        return Committee.builder()
                .id(Id.random())
                .status(Status.DRAFT)
                .name(name)
                .applicationStartDate(applicationStartDate)
                .applicationEndDate(applicationEndDate)
                .build();
    }

    public boolean areProjectQuestionsFixed() {
        return status.ordinal() >= Status.OPEN_TO_APPLICATIONS.ordinal();
    }

    public boolean areJuriesFixed() {
        return status.ordinal() >= Status.OPEN_TO_VOTES.ordinal();
    }

    public boolean areJuryCriteriaFixed() {
        return status.ordinal() >= Status.OPEN_TO_VOTES.ordinal();
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Committee.Id of(@NonNull final UUID uuid) {
            return Committee.Id.builder().uuid(uuid).build();
        }

        @JsonCreator
        public static Committee.Id of(@NonNull final String uuid) {
            return Committee.Id.of(UUID.fromString(uuid));
        }
    }


    public enum Status {
        DRAFT, OPEN_TO_APPLICATIONS, OPEN_TO_VOTES, CLOSED;
    }


    public record ProjectAnswer(@NonNull ProjectQuestion.Id projectQuestionId, String answer) {
    }

    public record Application(@NonNull UserId userId, @NonNull ProjectId projectId, @NonNull List<ProjectAnswer> answers) {
    }
}
