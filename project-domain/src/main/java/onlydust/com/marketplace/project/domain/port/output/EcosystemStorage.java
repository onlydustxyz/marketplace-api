package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

public interface EcosystemStorage {
    Page<Ecosystem> findAll(int pageIndex, int pageSize);
}
