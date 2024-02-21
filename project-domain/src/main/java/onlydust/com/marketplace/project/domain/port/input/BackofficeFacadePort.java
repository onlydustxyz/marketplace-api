package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.view.backoffice.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BackofficeFacadePort {
    Page<ProjectRepositoryView> getProjectRepositoryPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds);

    Page<ProjectBudgetView> getBudgetPage(int sanitizedPageIndex, int sanitizedPageSize, List<UUID> projectIds);

    Page<ProjectLeadInvitationView> getProjectLeadInvitationPage(int pageIndex, int pageSize, List<UUID> ids, List<UUID> projectIds);

    Page<UserView> listUsers(int pageIndex, int pageSize, UserView.Filters filters);

    Page<PaymentView> listPayments(int pageIndex, int pageSize, PaymentView.Filters filters);


    Page<EcosystemView> listEcosystems(int pageIndex, int pageSize, EcosystemView.Filters filters);

    Page<ProjectView> listProjects(int pageIndex, int pageSize, List<UUID> projectIds);

    Ecosystem createEcosystem(final Ecosystem ecosystem);

    SponsorView createSponsor(String name, URI url, URI logoUrl);

    SponsorView updateSponsor(UUID sponsorId, String name, URI url, URI logoUrl);

    Optional<SponsorView> getSponsor(UUID sponsorId);

    Page<SponsorView> listSponsors(int pageIndex, int pageSize, SponsorView.Filters filters);
}
