package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.view.backoffice.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "Backoffice"))
@AllArgsConstructor
public class BackofficeRestApi implements BackofficeApi {

    private final BackofficeFacadePort backofficeFacadePort;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    @Override
    public ResponseEntity<GithubRepositoryPage> getGithubRepositoryPage(Integer pageIndex, Integer pageSize,
                                                                        List<UUID> projectIds) {
        final int sanitizedPageSize = sanitizePageSize(pageSize, MAX_PAGE_SIZE);
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
    public ResponseEntity<EcosystemPage> getEcosystemPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds, List<UUID> ecosystemIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var filters = EcosystemView.Filters.builder()
                .projects(Optional.ofNullable(projectIds).orElse(List.of()))
                .ecosystems(Optional.ofNullable(ecosystemIds).orElse(List.of()))
                .build();

        final var ecosystemViewPage =
                backofficeFacadePort.listEcosystems(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), filters);

        final var response = mapEcosystemPageToContract(ecosystemViewPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProjectLeadInvitationPage> getProjectLeadInvitationPage(Integer pageIndex, Integer pageSize
            , List<UUID> ids, List<UUID> projectIds) {
        final int sanitizedPageSize = sanitizePageSize(pageSize, MAX_PAGE_SIZE);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        Page<ProjectLeadInvitationView> projectLeadInvitationViewPage =
                backofficeFacadePort.getProjectLeadInvitationPage(sanitizedPageIndex, sanitizedPageSize, ids, projectIds);

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
        final var usersPage = backofficeFacadePort.listUsers(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), filters);
        final var response = mapUserPageToContract(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<PaymentPage> getPaymentPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds, List<UUID> paymentIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var filters = PaymentView.Filters.builder()
                .projects(Optional.ofNullable(projectIds).orElse(List.of()))
                .payments(Optional.ofNullable(paymentIds).orElse(List.of()))
                .build();
        final var paymentsPage = backofficeFacadePort.listPayments(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), filters);
        final var response = mapPaymentPageToContract(paymentsPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProjectPage> getProjectPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var projectsPage = backofficeFacadePort.listProjects(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), projectIds);
        final var response = mapProjectPageToContract(projectsPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<EcosystemResponse> postEcosystem(EcosystemRequest ecosystemRequest) {
        final Ecosystem ecosystem = backofficeFacadePort.createEcosystem(mapEcosystemToDomain(ecosystemRequest));
        return ResponseEntity.ok(mapEcosystemToResponse(ecosystem));
    }
}
