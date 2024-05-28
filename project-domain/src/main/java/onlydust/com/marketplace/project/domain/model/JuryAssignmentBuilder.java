package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;
import lombok.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Value
public class JuryAssignmentBuilder {
    Committee.Id committeeId;
    UUID juryId;
    Integer votePerJury;
    Set<UUID> projectLeadOnProjectIds = new HashSet<>();
    Set<UUID> contributorOnProjectIds = new HashSet<>();
    Set<UUID> assignedOnProjectIds = new HashSet<>();

    public JuryAssignmentBuilder(@NonNull Committee.Id committeeId, @NonNull UUID juryId, @NonNull Integer votePerJury,
                                 @NonNull List<UUID> projectLeadOnProjectIds,
                                 @NonNull List<UUID> contributorOnProjectIds) {
        this.committeeId = committeeId;
        this.juryId = juryId;
        this.votePerJury = votePerJury;
        this.projectLeadOnProjectIds.addAll(projectLeadOnProjectIds);
        this.contributorOnProjectIds.addAll(contributorOnProjectIds);
    }

    public boolean canAssignProject(UUID projectId) {
        return this.assignedOnProjectIds.size() < this.votePerJury && !this.assignedOnProjectIds.contains(projectId)
                && !this.projectLeadOnProjectIds.contains(projectId) && !this.contributorOnProjectIds.contains(projectId);
    }

    public void assignProject(UUID projectId) {
        this.assignedOnProjectIds.add(projectId);
    }

    public List<JuryAssignment> buildForCriteria(final List<JuryCriteria> juryCriteria) {
        return this.assignedOnProjectIds.stream()
                .map(projectId -> JuryAssignment.virgin(this.juryId, this.committeeId, projectId, juryCriteria))
                .toList();
    }
}
