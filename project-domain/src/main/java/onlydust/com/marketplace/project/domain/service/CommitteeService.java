package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class CommitteeService implements CommitteeFacadePort {

    private final CommitteeStoragePort committeeStoragePort;

    @Override
    public Committee createCommittee(@NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        return committeeStoragePort.save(new Committee(name, startDate, endDate));
    }

    @Override
    public Page<CommitteeLinkView> getCommittees(Integer pageIndex, Integer pageSize) {
        return committeeStoragePort.findAll(pageIndex, pageSize);
    }

    @Override
    public void update(Committee committee) {
        committeeStoragePort.save(committee);
    }

    @Override
    public CommitteeView getCommitteeById(Committee.Id committeeId) {
        return committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));
    }
}
