package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Ecosystem;
import onlydust.com.marketplace.api.domain.port.input.EcosystemFacadePort;
import onlydust.com.marketplace.api.domain.port.output.EcosystemStorage;
import onlydust.com.marketplace.kernel.pagination.Page;

@AllArgsConstructor
public class EcosystemService implements EcosystemFacadePort {
    private final EcosystemStorage ecosystemStorage;

    @Override
    public Page<Ecosystem> findAll(int pageIndex, int pageSize) {
        return ecosystemStorage.findAll(pageIndex, pageSize);
    }
}
