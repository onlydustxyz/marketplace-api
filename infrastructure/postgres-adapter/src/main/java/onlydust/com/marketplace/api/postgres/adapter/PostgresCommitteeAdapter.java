package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;

@AllArgsConstructor
public class PostgresCommitteeAdapter implements CommitteeStoragePort {

    private final CommitteeRepository committeeRepository;

    @Override
    public Committee save(Committee committee) {
        return committeeRepository.save(CommitteeEntity.fromDomain(committee)).toDomain();
    }
}
