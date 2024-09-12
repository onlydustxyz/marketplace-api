package onlydust.com.marketplace.project.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import static java.util.stream.Collectors.toMap;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JuryAssignment {
    @NonNull
    UserId juryId;
    @NonNull
    Committee.Id committeeId;
    @NonNull
    ProjectId projectId;
    @NonNull
    Map<JuryCriteria.Id, Optional<Integer>> votes;

    public static JuryAssignment virgin(@NonNull UserId juryId,
                                        @NonNull Committee.Id committeeId,
                                        @NonNull ProjectId projectId,
                                        @NonNull List<JuryCriteria> juryCriteria) {
        return new JuryAssignment(juryId,
                committeeId,
                projectId,
                juryCriteria.stream().collect(toMap(JuryCriteria::id, c -> Optional.empty()))
        );
    }

    public static JuryAssignment withVotes(@NonNull UserId juryId,
                                           @NonNull Committee.Id committeeId,
                                           @NonNull ProjectId projectId,
                                           @NonNull Map<JuryCriteria.Id, Integer> votes) {
        return new JuryAssignment(juryId,
                committeeId,
                projectId,
                votes.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> Optional.ofNullable(e.getValue())))
        );
    }

    public OptionalDouble getScore() {
        return votes.values().stream()
                .filter(Optional::isPresent)
                .mapToInt(Optional::get)
                .average();
    }

    public boolean scoreMissing() {
        return votes.values().stream().allMatch(Optional::isEmpty);
    }
}
