package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.EcosystemFacadePort;
import onlydust.com.marketplace.project.domain.port.output.EcosystemStorage;

@AllArgsConstructor
public class EcosystemService implements EcosystemFacadePort {
    private final EcosystemStorage ecosystemStorage;

    @Override
    public Page<Ecosystem> findAll(int pageIndex, int pageSize) {
        return ecosystemStorage.findAll(pageIndex, pageSize);
    }
}
