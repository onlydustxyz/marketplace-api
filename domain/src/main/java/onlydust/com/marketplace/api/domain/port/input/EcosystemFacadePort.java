package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.Ecosystem;
import onlydust.com.marketplace.kernel.pagination.Page;

public interface EcosystemFacadePort {

    Page<Ecosystem> findAll(int pageIndex, int pageSize);
}
