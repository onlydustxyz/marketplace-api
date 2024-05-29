package onlydust.com.marketplace.project.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JuryAssignment {
    @NonNull
    UUID juryId;
    @NonNull
    Committee.Id committeeId;
    @NonNull
    UUID projectId;
    @NonNull
    Map<JuryCriteria.Id, Optional<Integer>> votes;

    public static JuryAssignment virgin(@NonNull UUID juryId,
                                        @NonNull Committee.Id committeeId,
                                        @NonNull UUID projectId,
                                        @NonNull List<JuryCriteria> juryCriteria) {
        return new JuryAssignment(juryId,
                committeeId,
                projectId,
                juryCriteria.stream().collect(toMap(JuryCriteria::id, c -> Optional.empty()))
        );
    }

    public static JuryAssignment withVotes(@NonNull UUID juryId,
                                           @NonNull Committee.Id committeeId,
                                           @NonNull UUID projectId,
                                           @NonNull Map<JuryCriteria.Id, Integer> votes) {
        return new JuryAssignment(juryId,
                committeeId,
                projectId,
                votes.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> Optional.of(e.getValue())))
        );
    }

    public double getScore() {
        return votes.values().stream()
                .filter(Optional::isPresent)
                .mapToInt(Optional::get)
                .average()
                .orElseThrow(() -> internalServerError("Cannot compute score for project %s".formatted(projectId)));
    }
}
