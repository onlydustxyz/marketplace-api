package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.time.ZonedDateTime;

public interface CommitteeFacadePort {
    Committee createCommittee(@NonNull String name,
                              @NonNull ZonedDateTime startDate,
                              @NonNull ZonedDateTime endDate);

    Page<CommitteeLinkView> getCommittees(Integer pageIndex, Integer pageSize);

    void update(Committee committee);

    CommitteeView getCommitteeById(Committee.Id committeeId);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    void createUpdateApplicationForCommittee(Committee.Id committeeId, Committee.Application application);
}
