package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BackofficeFacadePort {

    Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters);

    Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search);

    Ecosystem createEcosystem(final Ecosystem ecosystem);

    BoSponsorView createSponsor(String name, URI url, URI logoUrl);

    BoSponsorView updateSponsor(UUID sponsorId, String name, URI url, URI logoUrl);

    Optional<BoSponsorView> getSponsor(UUID sponsorId);

    Page<BoSponsorView> listSponsors(int pageIndex, int pageSize, BoSponsorView.Filters filters);
}
