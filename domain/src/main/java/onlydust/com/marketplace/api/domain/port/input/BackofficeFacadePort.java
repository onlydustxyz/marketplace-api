package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.util.List;
import java.util.UUID;

public interface BackofficeFacadePort {
    Page<ProjectRepositoryView> getProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds);

    Page<ProjectBudgetView> getBudgetPage(int sanitizedPageIndex, int sanitizedPageSize, List<UUID> projectIds);
}
