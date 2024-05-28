package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.view.backoffice.BoSponsorView;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;

import java.util.Optional;
import java.util.UUID;

public interface BackofficeStoragePort {

    Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search);

    Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters);

    Ecosystem createEcosystem(Ecosystem ecosystem);

    void saveSponsor(Sponsor sponsor);

    Optional<BoSponsorView> getSponsor(UUID sponsorId);

    Page<BoSponsorView> listSponsors(int pageIndex, int pageSize, BoSponsorView.Filters filters);
}
