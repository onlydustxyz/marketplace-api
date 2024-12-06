package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadRecommendationsApi;
import onlydust.com.marketplace.api.contract.model.RecommendedProjectsResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectPageItemQueryEntity;
import onlydust.com.marketplace.api.read.repositories.ProjectsPageRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.port.input.RecommendationFacadePort;
import org.springframework.context.annotation.Profile;
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
    private final ProjectsPageRepository projectsPageRepository;

    @Override
    public ResponseEntity<RecommendedProjectsResponse> getRecommendedProjects(String version) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var recommendedProjectIds = recommendationFacadePort.getRecommendedProjects(authenticatedUser.id(), version);
        final var projects = projectsPageRepository.findAllByIdIn(authenticatedUser.id().value(),
                recommendedProjectIds.stream().map(ProjectId::value).toArray(UUID[]::new));
        return ResponseEntity.ok(new RecommendedProjectsResponse()
                .projects(projects.stream().map(ProjectPageItemQueryEntity::toShortResponseV2).toList()));
    }
}
