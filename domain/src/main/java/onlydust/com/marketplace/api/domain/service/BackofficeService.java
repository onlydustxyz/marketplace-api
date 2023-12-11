package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.view.backoffice.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.util.List;
import java.util.UUID;

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
    public Page<ProjectLeadInvitationView> getProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids) {
        return backofficeStoragePort.findProjectLeadInvitationPage(pageIndex, pageSize, ids);
    }

    @Override
    public Page<PaymentView> listPayments(int pageIndex, int pageSize, List<UUID> projectIds) {
        return backofficeStoragePort.listPayments(pageIndex, pageSize, projectIds);
    }

    @Override
    public Page<ProjectView> listProjects(int pageIndex, int pageSize) {
        return backofficeStoragePort.listProjects(pageIndex, pageSize);
    }
}
