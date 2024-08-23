package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;

import java.net.URI;
import java.util.UUID;

public interface BackofficeFacadePort {

    Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters);

    Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search);

    Ecosystem createEcosystem(final Ecosystem ecosystem);

    Sponsor createSponsor(String name, URI url, URI logoUrl);

    void updateSponsor(UUID sponsorId, String name, URI url, URI logoUrl);
}
