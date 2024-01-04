package onlydust.com.marketplace.api.domain.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectLeadInvitationView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectView;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.domain.view.backoffice.UserView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

@AllArgsConstructor
public class BackofficeService implements BackofficeFacadePort {

  final BackofficeStoragePort backofficeStoragePort;

  @Override
  public Page<ProjectRepositoryView> getProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds) {
    return backofficeStoragePort.findProjectRepositoryPage(pageIndex, pageSize, projectIds);
  }

  @Override
  public Page<ProjectBudgetView> getBudgetPage(int pageIndex, int pageSize, List<UUID> projectIds) {
    return backofficeStoragePort.findProjectBudgetPage(pageIndex, pageSize, projectIds);
  }

  @Override
  public Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters) {
    return backofficeStoragePort.listSponsors(pageIndex, pageSize, filters);
  }

  @Override
  public Page<ProjectLeadInvitationView> getProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids, List<UUID> projectIds) {
    return backofficeStoragePort.findProjectLeadInvitationPage(pageIndex, pageSize, ids, projectIds);
  }

  @Override
  public Page<UserView> listUsers(int pageIndex, int pageSize, UserView.Filters filters) {
    return backofficeStoragePort.listUsers(pageIndex, pageSize, filters);
  }

  @Override
  public Page<PaymentView> listPayments(int pageIndex, int pageSize, PaymentView.Filters filters) {
    return backofficeStoragePort.listPayments(pageIndex, pageSize, filters);
  }

  @Override
  public Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds) {
    return backofficeStoragePort.listProjects(pageIndex, pageSize, projectIds);
  }
}
