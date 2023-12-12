package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectLeadInvitationView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.backoffice.UserView;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageSize;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;

@RestController
@Tags(@Tag(name = "Backoffice"))
@AllArgsConstructor
public class BackofficeRestApi implements BackofficeApi {

    private final BackofficeFacadePort backofficeFacadePort;

    @Override
    public ResponseEntity<GithubRepositoryPage> getGithubRepositoryPage(Integer pageIndex, Integer pageSize,
                                                                        List<UUID> projectIds) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        Page<ProjectRepositoryView> projectRepositoryViewPage =
                backofficeFacadePort.getProjectRepositoryPage(sanitizedPageIndex, sanitizedPageSize, projectIds);

        final GithubRepositoryPage githubRepositoryPage = mapGithubRepositoryPageToResponse(projectRepositoryViewPage
                , sanitizedPageIndex);

        return githubRepositoryPage.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(githubRepositoryPage) :
                ResponseEntity.ok(githubRepositoryPage);
    }

    @Override
    public ResponseEntity<SponsorPage> getSponsorPage(Integer pageIndex, Integer pageSize,
                                                      List<UUID> projectIds, List<UUID> sponsorIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var filters = SponsorView.Filters.builder()
                .projects(Optional.ofNullable(projectIds).orElse(List.of()))
                .sponsors(Optional.ofNullable(sponsorIds).orElse(List.of()))
                .build();

        final var sponsorPage =
                backofficeFacadePort.listSponsors(sanitizedPageIndex, sanitizePageSize(pageSize), filters);

        final var response = mapSponsorPageToContract(sponsorPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<BudgetPage> getBudgetPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        Page<ProjectBudgetView> budgetViewPage =
                backofficeFacadePort.getBudgetPage(sanitizedPageIndex, sanitizedPageSize, projectIds);

        final BudgetPage budgetPage = mapBudgetPageToResponse(budgetViewPage
                , sanitizedPageIndex);

        return budgetPage.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(budgetPage) :
                ResponseEntity.ok(budgetPage);
    }

    @Override
    public ResponseEntity<ProjectLeadInvitationPage> getProjectLeadInvitationPage(Integer pageIndex, Integer pageSize
            , List<UUID> ids) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        Page<ProjectLeadInvitationView> projectLeadInvitationViewPage =
                backofficeFacadePort.getProjectLeadInvitationPage(sanitizedPageIndex, sanitizedPageSize, ids);

        final ProjectLeadInvitationPage projectLeadInvitationPage =
                mapProjectLeadInvitationPageToContract(projectLeadInvitationViewPage
                        , sanitizedPageIndex);

        return projectLeadInvitationPage.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(projectLeadInvitationPage) :
                ResponseEntity.ok(projectLeadInvitationPage);
    }

    @Override
    public ResponseEntity<UserPage> getUserPage(Integer pageIndex, Integer pageSize, List<UUID> userIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var filters = UserView.Filters.builder()
                .users(Optional.ofNullable(userIds).orElse(List.of()))
                .build();
        final var usersPage = backofficeFacadePort.listUsers(sanitizedPageIndex, sanitizePageSize(pageSize), filters);
        final var response = mapUserPageToContract(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PaymentPage> getPaymentPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var paymentsPage = backofficeFacadePort.listPayments(sanitizedPageIndex, sanitizePageSize(pageSize), projectIds);
        final var response = mapPaymentPageToContract(paymentsPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProjectPage> getProjectPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var paymentsPage = backofficeFacadePort.listProjects(sanitizedPageIndex, sanitizePageSize(pageSize), projectIds);
        final var response = mapProjectPageToContract(paymentsPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

}
