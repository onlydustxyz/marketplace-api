package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

public interface BackofficeFacadePort {
    Page<ProjectRepositoryView> getProjectRepositoryPage(Integer pageIndex, Integer pageSize);
}
