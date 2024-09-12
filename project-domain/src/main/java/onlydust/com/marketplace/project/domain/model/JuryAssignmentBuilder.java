package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value
public class JuryAssignmentBuilder {
    Committee.Id committeeId;
    UserId juryId;
    Integer votePerJury;
    Set<ProjectId> projectLeadOnProjectIds = new HashSet<>();
    Set<ProjectId> contributorOnProjectIds = new HashSet<>();
    Set<ProjectId> assignedOnProjectIds = new HashSet<>();

    public JuryAssignmentBuilder(@NonNull Committee.Id committeeId, @NonNull UserId juryId, @NonNull Integer votePerJury,
                                 @NonNull List<ProjectId> projectLeadOnProjectIds,
                                 @NonNull List<ProjectId> contributorOnProjectIds) {
        this.committeeId = committeeId;
        this.juryId = juryId;
        this.votePerJury = votePerJury;
        this.projectLeadOnProjectIds.addAll(projectLeadOnProjectIds);
        this.contributorOnProjectIds.addAll(contributorOnProjectIds);
    }

    public boolean canAssignProject(ProjectId projectId) {
        return this.assignedOnProjectIds.size() < this.votePerJury && !this.assignedOnProjectIds.contains(projectId)
               && !this.projectLeadOnProjectIds.contains(projectId) && !this.contributorOnProjectIds.contains(projectId);
    }

    public void assignProject(ProjectId projectId) {
        this.assignedOnProjectIds.add(projectId);
    }

    public List<JuryAssignment> buildForCriteria(final List<JuryCriteria> juryCriteria) {
        return this.assignedOnProjectIds.stream()
                .map(projectId -> JuryAssignment.virgin(this.juryId, this.committeeId, projectId, juryCriteria))
                .toList();
    }
}
