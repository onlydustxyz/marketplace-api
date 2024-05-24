package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@Getter
public class JuryAssignment {
    @NonNull
    UUID projectId;
    @NonNull
    Committee.Id committeeId;
    @NonNull
    UUID juryId;
    @NonNull
    List<JuryVote> votes;

    public JuryAssignment(@NonNull UUID projectId, @NonNull Committee.Id committeeId, @NonNull UUID juryId, @NonNull List<JuryCriteria> juryCriteria) {
        this.projectId = projectId;
        this.committeeId = committeeId;
        this.juryId = juryId;
        this.votes = juryCriteria.stream().map(jc -> JuryVote.builder().criteriaId(jc.id()).build()).toList();
    }

    @Builder
    @Getter
    public static class JuryVote {
        @NonNull
        JuryCriteria.Id criteriaId;
        Integer score;
    }
}
