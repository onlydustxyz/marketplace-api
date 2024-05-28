package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignment;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommitteeStoragePort {
    Committee save(Committee committee);

    Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize);

    Optional<CommitteeView> findViewById(Committee.Id committeeId);

    Optional<Committee> findById(Committee.Id committeeId);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    List<ProjectAnswerView> getApplicationAnswers(Committee.Id committeeId, UUID projectId);

    Optional<CommitteeApplicationDetailsView> findByCommitteeIdAndProjectId(Committee.Id committeeId, UUID projectId);

    void saveJuryAssignments(List<JuryAssignment> juryAssignments);

    void saveJuryAssignment(JuryAssignment juryAssignment);
}
