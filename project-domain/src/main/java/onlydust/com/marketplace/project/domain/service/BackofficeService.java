package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;

@AllArgsConstructor
public class BackofficeService implements BackofficeFacadePort {

    final BackofficeStoragePort backofficeStoragePort;

    @Override
    public Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters) {
        return backofficeStoragePort.listEcosystems(pageIndex, pageSize, filters);
    }

    @Override
    public Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search) {
        return backofficeStoragePort.searchProjects(pageIndex, pageSize, search);
    }

    @Override
    public Ecosystem createEcosystem(Ecosystem ecosystem) {
        return backofficeStoragePort.createEcosystem(ecosystem);
    }
}
