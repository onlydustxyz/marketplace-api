package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeApi;
import onlydust.com.backoffice.api.contract.model.EcosystemPage;
import onlydust.com.backoffice.api.contract.model.EcosystemRequest;
import onlydust.com.backoffice.api.contract.model.EcosystemResponse;
import onlydust.com.backoffice.api.contract.model.ProjectPage;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import org.springframework.context.annotation.Profile;
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
@Profile("bo")
public class BackofficeRestApi implements BackofficeApi {
    private final BackofficeFacadePort backofficeFacadePort;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

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
    public ResponseEntity<ProjectPage> getProjectPage(Integer pageIndex, Integer pageSize, String search) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var projectsPage = backofficeFacadePort.searchProjects(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), search);
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
