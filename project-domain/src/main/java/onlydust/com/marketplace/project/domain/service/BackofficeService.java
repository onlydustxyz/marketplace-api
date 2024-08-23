package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;

import java.net.URI;
import java.util.UUID;

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

    @Override
    public Sponsor createSponsor(String name, URI url, URI logoUrl) {
        final var sponsor = Sponsor.builder()
                .id(UUID.randomUUID())
                .name(name)
                .url(url.toString())
                .logoUrl(logoUrl.toString())
                .build();
        backofficeStoragePort.saveSponsor(sponsor);
        return sponsor;
    }

    @Override
    public void updateSponsor(UUID sponsorId, String name, URI url, URI logoUrl) {
        backofficeStoragePort.saveSponsor(Sponsor.builder()
                .id(sponsorId)
                .name(name)
                .url(url.toString())
                .logoUrl(logoUrl.toString())
                .build());
    }
}
