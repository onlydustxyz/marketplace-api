package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignment;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CommitteeStoragePort {
    Committee save(Committee committee);

    Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize);

    Optional<Committee> findById(Committee.Id committeeId);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    void saveJuryAssignments(List<JuryAssignment> juryAssignments);

    void saveJuryVotes(UUID juryId, Committee.Id committeeId, UUID projectId, Map<JuryCriteria.Id, Integer> votes);

    List<JuryAssignment> findJuryAssignments(Committee.Id committeeId);

    void saveAllocations(Committee.Id committeeId, UUID currencyId, Map<UUID, BigDecimal> projectAllocations);
}
