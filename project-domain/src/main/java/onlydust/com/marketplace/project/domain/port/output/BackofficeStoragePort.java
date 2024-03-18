package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BackofficeStoragePort {
    Page<ProjectRepositoryView> findProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds);

    Page<ProjectLeadInvitationView> findProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids, List<UUID> projectIds);

    Page<UserView> listUsers(int pageIndex, int pageSize, UserView.Filters filters);

    Page<OldProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds);

    Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search);

    Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters);

    Ecosystem createEcosystem(Ecosystem ecosystem);

    void saveSponsor(Sponsor sponsor);

    Optional<SponsorView> getSponsor(UUID sponsorId);

    Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters);
}
