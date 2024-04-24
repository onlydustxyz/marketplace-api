package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class BackofficeService implements BackofficeFacadePort {

    final BackofficeStoragePort backofficeStoragePort;

    @Override
    public Page<ProjectRepositoryView> getProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds) {
        return backofficeStoragePort.findProjectRepositoryPage(pageIndex, pageSize, projectIds);
    }

    @Override
    public Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters) {
        return backofficeStoragePort.listEcosystems(pageIndex, pageSize, filters);
    }

    @Override
    public Page<ProjectLeadInvitationView> getProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids, List<UUID> projectIds) {
        return backofficeStoragePort.findProjectLeadInvitationPage(pageIndex, pageSize, ids, projectIds);
    }

    @Override
    public Page<UserShortView> listUsers(int pageIndex, int pageSize, UserShortView.Filters filters) {
        return backofficeStoragePort.listUsers(pageIndex, pageSize, filters);
    }

    @Override
    public Page<OldProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds) {
        return backofficeStoragePort.listProjects(pageIndex, pageSize, projectIds);
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
    public BoSponsorView createSponsor(String name, URI url, URI logoUrl) {
        final var sponsorId = UUID.randomUUID();
        backofficeStoragePort.saveSponsor(Sponsor.builder()
                .id(sponsorId)
                .name(name)
                .url(url.toString())
                .logoUrl(logoUrl.toString())
                .build());
        return getSponsor(sponsorId)
                .orElseThrow(() -> internalServerError("Sponsor not properly created"));
    }

    @Override
    public BoSponsorView updateSponsor(UUID sponsorId, String name, URI url, URI logoUrl) {
        backofficeStoragePort.saveSponsor(Sponsor.builder()
                .id(sponsorId)
                .name(name)
                .url(url.toString())
                .logoUrl(logoUrl.toString())
                .build());
        return getSponsor(sponsorId)
                .orElseThrow(() -> internalServerError("Sponsor not properly updated"));
    }

    @Override
    public Optional<BoSponsorView> getSponsor(UUID sponsorId) {
        return backofficeStoragePort.getSponsor(sponsorId);
    }

    @Override
    public Page<BoSponsorView> listSponsors(int pageIndex, int pageSize, BoSponsorView.Filters filters) {
        return backofficeStoragePort.listSponsors(pageIndex, pageSize, filters);
    }
}
