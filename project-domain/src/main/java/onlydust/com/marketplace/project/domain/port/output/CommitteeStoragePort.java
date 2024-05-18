package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.util.Optional;

public interface CommitteeStoragePort {
    Committee save(Committee committee);

    Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize);

    Optional<CommitteeView> findById(Committee.Id committeeId);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    void saveApplication(Committee.Id committeeId, Committee.Application application);
}
