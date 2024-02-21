package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

public interface EcosystemFacadePort {

    Page<Ecosystem> findAll(int pageIndex, int pageSize);
}
