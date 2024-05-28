package onlydust.com.marketplace.project.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public static JuryAssignment virgin(@NonNull UUID juryId, @NonNull Committee.Id committeeId, @NonNull UUID projectId, @NonNull List<JuryCriteria> juryCriteria) {
        return new JuryAssignment(juryId,
                committeeId,
                projectId,
                juryCriteria.stream().collect(Collectors.toMap(JuryCriteria::id, c -> Optional.empty()))
        );
    }
}
