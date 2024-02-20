package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Ecosystem;
import onlydust.com.marketplace.kernel.pagination.Page;

public interface EcosystemStorage {
    Page<Ecosystem> findAll(int pageIndex, int pageSize);
}
