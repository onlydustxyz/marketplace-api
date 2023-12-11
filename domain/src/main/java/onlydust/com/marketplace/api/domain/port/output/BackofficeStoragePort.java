package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.backoffice.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.util.List;
import java.util.UUID;

public interface BackofficeStoragePort {
    Page<ProjectRepositoryView> findProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds);

    Page<ProjectBudgetView> findProjectBudgetPage(int pageIndex, int pageSize, List<UUID> projectIds);

    Page<ProjectLeadInvitationView> findProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids);

    Page<PaymentView> listPayments(int pageIndex, int pageSize, List<UUID> projectIds);

    Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds);
}
