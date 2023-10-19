package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.ProjectsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ContributorSearchResponseMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageSize;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectContributorsMapper.mapProjectContributorsLinkViewPageToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectContributorsMapper.mapSortBy;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.*;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
@Slf4j
public class ProjectsRestApi implements ProjectsApi {

    private final ProjectFacadePort projectFacadePort;
    private final ContributorFacadePort contributorFacadePort;
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<ProjectResponse> getProject(final UUID projectId) {
        final var project = projectFacadePort.getById(projectId);
        final var projectResponse = mapProjectDetails(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectResponse> getProjectBySlug(final String slug) {
        final var project = projectFacadePort.getBySlug(slug);
        final var projectResponse = mapProjectDetails(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectListResponse> getProjects(final String sort, final List<String> technologies,
                                                           final List<String> sponsors, final Boolean mine,
                                                           final String search) {
        final Optional<User> optionalUser = authenticationService.tryGetAuthenticatedUser();
        final ProjectCardView.SortBy sortBy = mapSortByParameter(sort);
        final Page<ProjectCardView> projectCardViewPage =
                optionalUser.map(user -> projectFacadePort.getByTechnologiesSponsorsUserIdSearchSortBy(technologies,
                                sponsors, search, sortBy, user.getId(), isNull(mine) ? false : mine))
                        .orElseGet(() -> projectFacadePort.getByTechnologiesSponsorsSearchSortBy(technologies, sponsors,
                                search, sortBy));
        return ResponseEntity.ok(mapProjectCards(projectCardViewPage));
    }

    @Override
    public ResponseEntity<CreateProjectResponse> createProject(CreateProjectRequest createProjectRequest) {
        final UUID projectId = projectFacadePort.createProject(
                CreateProjectCommand.builder()
                        .name(createProjectRequest.getName())
                        .shortDescription(createProjectRequest.getShortDescription())
                        .longDescription(createProjectRequest.getLongDescription())
                        .githubUserIdsAsProjectLeads(createProjectRequest.getInviteGithubUserIdsAsProjectLeads())
                        .githubRepoIds(createProjectRequest.getGithubRepoIds())
                        .isLookingForContributors(createProjectRequest.getIsLookingForContributors())
                        .moreInfos(createProjectRequest.getMoreInfo().stream()
                                .map(moreInfo -> CreateProjectCommand.MoreInfo.builder()
                                        .url(moreInfo.getUrl())
                                        .value(moreInfo.getValue())
                                        .build())
                                .toList())
                        .imageUrl(createProjectRequest.getLogoUrl())
                        .build()
        );

        final CreateProjectResponse createProjectResponse = new CreateProjectResponse();
        createProjectResponse.setProjectId(projectId);
        return ResponseEntity.ok(createProjectResponse);
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadProjectLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw OnlyDustException.invalidInput("Error while reading image data", e);
        }

        final URL imageUrl = projectFacadePort.saveLogoImage(imageInputStream);
        final UploadImageResponse response = new UploadImageResponse();
        response.url(imageUrl.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ContributorSearchResponse> searchProjectContributors(UUID projectId, String login) {
        final var contributors = contributorFacadePort.searchContributors(projectId, login);
        return ResponseEntity.ok(ContributorSearchResponseMapper.of(contributors.getLeft(), contributors.getRight()));
    }

    @Override
    public ResponseEntity<ContributorsPageResponse> getProjectContributors(UUID projectId, Integer pageIndex,
                                                                          Integer pageSize, String sort) {

        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final ProjectContributorsLinkView.SortBy sortBy = mapSortBy(sort);
        final Page<ProjectContributorsLinkView> projectContributorsLinkViewPage =
                authenticationService.tryGetAuthenticatedUser().map(user -> projectFacadePort.getContributorsForProjectLeadId(projectId, sortBy, user.getId(), pageIndex,
                sanitizedPageSize)).orElseGet(() -> projectFacadePort.getContributors(projectId, sortBy, pageIndex,
                        sanitizedPageSize));
        return ResponseEntity.ok(mapProjectContributorsLinkViewPageToResponse(projectContributorsLinkViewPage, pageIndex));
    }


}
