package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.backoffice.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.util.List;
import java.util.UUID;

public interface BackofficeStoragePort {
    Page<ProjectRepositoryView> findProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds);

    Page<ProjectBudgetView> findProjectBudgetPage(int pageIndex, int pageSize, List<UUID> projectIds);

    Page<ProjectLeadInvitationView> findProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids, List<UUID> projectIds);

    Page<UserView> listUsers(int pageIndex, int pageSize, UserView.Filters filters);

    Page<PaymentView> listPayments(int pageIndex, int pageSize, PaymentView.Filters filters);

    Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters);

    Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds);
}
