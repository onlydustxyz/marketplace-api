package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.time.ZonedDateTime;
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
}
