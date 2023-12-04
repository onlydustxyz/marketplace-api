package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

public interface BackofficeStoragePort {
    Page<ProjectRepositoryView> findProjectRepositoryPage(Integer pageIndex, Integer pageSize);
}
