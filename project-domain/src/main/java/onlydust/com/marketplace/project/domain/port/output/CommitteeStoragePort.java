package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignment;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeView;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommitteeStoragePort {
    Committee save(Committee committee);

    Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize);

    Optional<CommitteeView> findViewById(Committee.Id committeeId);

    Optional<Committee> findById(Committee.Id committeeId);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    void saveApplication(Committee.Id committeeId, Committee.Application application);

    List<ProjectAnswerView> getApplicationAnswers(Committee.Id committeeId, UUID projectId);

    boolean hasStartedApplication(Committee.Id committeeId, Committee.Application application);

    Optional<CommitteeApplicationDetailsView> findByCommitteeIdAndProjectId(Committee.Id committeeId, UUID projectId);

    void deleteAllJuries(Committee.Id committeeId);

    void saveJuries(Committee.Id committeeId, List<UUID> juryIds);

    void deleteAllJuryCriteria(Committee.Id committeeId);

    void saveJuryCriteria(Committee.Id committeeId, List<JuryCriteria> juryCriteria);

    void saveJuryAssignments(List<JuryAssignment> juryAssignments);
}
