package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ProjectsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectLeadView;
import onlydust.com.marketplace.api.domain.view.ProjectView;
import onlydust.com.marketplace.api.domain.view.SponsorView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.projectToResponse;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
public class ProjectRestApi implements ProjectsApi {

    private final ProjectFacadePort projectFacadePort;

    @Override
    public ResponseEntity<ShortProjectResponse> getProject(final UUID projectId) {
        final Project project = projectFacadePort.getById(projectId);
        final ShortProjectResponse projectResponse = projectToResponse(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ShortProjectResponse> getProjectBySlug(final String slug) {
        final Project project = projectFacadePort.getBySlug(slug);
        final ShortProjectResponse projectResponse = projectToResponse(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectListResponse> getProjects(final String sort, final List<String> technology,
                                                           final List<String> sponsor, final String ownership,
                                                           final String search) {
        final Page<ProjectView> projectViewPage =
                projectFacadePort.getByTechnologiesSponsorsOwnershipSearchSortBy(technology, sponsor, ownership,
                        search, sort);
        final ProjectListResponse projectListResponse = new ProjectListResponse();
        final List<ProjectListItemResponse> projectListItemResponses = new ArrayList<>();
        for (ProjectView projectView : projectViewPage.getContent()) {
            final ProjectListItemResponse projectListItemResponse = new ProjectListItemResponse();
            projectListItemResponse.setId(projectView.getId());
            projectListItemResponse.setName(projectView.getName());
            projectListItemResponse.setLogoUrl(projectView.getLogoUrl());
            projectListItemResponse.setPrettyId(projectView.getSlug());
            projectListItemResponse.setHiring(projectView.getHiring());
            projectListItemResponse.setShortDescription(projectView.getShortDescription());

            for (ProjectLeadView projectLeadView : projectView.getProjectLeadViews()) {
                final RegisteredUserMinimalistResponse registeredUserMinimalistResponse =
                        new RegisteredUserMinimalistResponse();
                registeredUserMinimalistResponse.setId(projectView.getId());
                registeredUserMinimalistResponse.setAvatarUrl(projectLeadView.getAvatarUrl());
                registeredUserMinimalistResponse.setLogin(projectLeadView.getLogin());
                projectListItemResponse.addLeadersItem(registeredUserMinimalistResponse);
            }

            for (SponsorView sponsorView : projectView.getSponsors()) {
                final SponsorResponse sponsorResponse = new SponsorResponse();
                sponsorResponse.setId(sponsorView.getId());
                sponsorResponse.setName(sponsorView.getName());
                sponsorResponse.setLogoUrl(sponsorView.getLogoUrl());
                projectListItemResponse.addSponsorsItem(sponsorResponse);
            }

            projectListItemResponses.add(projectListItemResponse);
        }
        projectListResponse.setProjects(projectListItemResponses);
        return ResponseEntity.ok(projectListResponse);
    }
}
