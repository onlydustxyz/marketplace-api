package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeView;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CommitteeFacadePort {
    Committee createCommittee(@NonNull String name,
                              @NonNull ZonedDateTime startDate,
                              @NonNull ZonedDateTime endDate);

    Page<CommitteeLinkView> getCommittees(Integer pageIndex, Integer pageSize);

    void update(Committee committee);

    CommitteeView getCommitteeById(Committee.Id committeeId);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    void createUpdateApplicationForCommittee(Committee.Id committeeId, Committee.Application application);

    CommitteeApplicationView getCommitteeApplication(Committee.Id committeeId, Optional<UUID> projectId, UUID userId);

    CommitteeApplicationDetailsView getCommitteeApplicationDetails(Committee.Id committeeId, UUID projectId);

    void vote(UUID juryId, Committee.Id committeeId, UUID projectId, Map<JuryCriteria.Id, Integer> scores);

    void allocate(Committee.Id committeeId, UUID currencyId, BigDecimal totalAmount, BigDecimal minAllocation, BigDecimal maxAllocation);

    void saveAllocations(Committee.Id committeeId, UUID currencyId, Map<UUID, BigDecimal> projectAllocations);
}
