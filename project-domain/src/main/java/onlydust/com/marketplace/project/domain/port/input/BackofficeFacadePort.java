package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;

public interface BackofficeFacadePort {
    Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search);
}
