package onlydust.com.marketplace.api.domain.port.input;

import java.util.List;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectLeadInvitationView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectView;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.domain.view.backoffice.UserView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

public interface BackofficeFacadePort {

  Page<ProjectRepositoryView> getProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds);

  Page<ProjectBudgetView> getBudgetPage(int sanitizedPageIndex, int sanitizedPageSize, List<UUID> projectIds);

  Page<ProjectLeadInvitationView> getProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids, List<UUID> projectIds);

  Page<UserView> listUsers(int pageIndex, int pageSize, UserView.Filters filters);

  Page<PaymentView> listPayments(int pageIndex, int pageSize, PaymentView.Filters filters);

  Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters);

  Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds);
}
