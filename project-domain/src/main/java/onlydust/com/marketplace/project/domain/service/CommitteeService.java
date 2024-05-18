package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class CommitteeService implements CommitteeFacadePort {

    private final CommitteeStoragePort committeeStoragePort;

    @Override
    public Committee createCommittee(@NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        return committeeStoragePort.save(new Committee(name, startDate, endDate));
    }
}
