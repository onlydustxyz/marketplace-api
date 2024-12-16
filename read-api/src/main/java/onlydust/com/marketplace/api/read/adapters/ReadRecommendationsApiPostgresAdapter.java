package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadRecommendationsApi;
import onlydust.com.marketplace.api.contract.model.RecommendedProjectsResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectPageItemQueryEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectPageV2ItemQueryEntity;
import onlydust.com.marketplace.api.read.repositories.ProjectsPageV2Repository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.port.input.RecommendationFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadRecommendationsApiPostgresAdapter implements ReadRecommendationsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final RecommendationFacadePort recommendationFacadePort;
    private final ProjectsPageV2Repository projectsPageV2Repository;

    @Override
    public ResponseEntity<RecommendedProjectsResponse> getRecommendedProjects(String version) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var recommendedProjectIds = recommendationFacadePort.getRecommendedProjects(authenticatedUser.id(), version);
        final var projects = projectsPageV2Repository.findAll(
                recommendedProjectIds.stream().map(ProjectId::value).toArray(UUID[]::new),
                null,
                null,
                null,
                null,
                Pageable.unpaged()
        );
        return ResponseEntity.ok(new RecommendedProjectsResponse()
                .projects(projects.stream().map(ProjectPageV2ItemQueryEntity::toShortResponse).toList()));
    }
}
