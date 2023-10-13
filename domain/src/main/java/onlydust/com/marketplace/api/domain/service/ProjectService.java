package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ProjectService implements ProjectFacadePort {

    private final ProjectStoragePort projectStoragePort;
    private final ImageStoragePort imageStoragePort;
    private final UUIDGeneratorPort uuidGeneratorPort;

    @Override
    public ProjectDetailsView getById(UUID projectId) {
        return projectStoragePort.getById(projectId);
    }

    @Override
    public ProjectDetailsView getBySlug(String slug) {
        return projectStoragePort.getBySlug(slug);
    }

    @Override
    public Page<ProjectCardView> getByTechnologiesSponsorsUserIdSearchSortBy(List<String> technology,
                                                                             List<String> sponsor, UUID userId,
                                                                             String search, ProjectCardView.SortBy sort) {
        return projectStoragePort.findByTechnologiesSponsorsUserIdSearchSortBy(technology, sponsor, userId,
                search, sort);
    }

    @Override
    public UUID createProject(CreateProjectCommand command) {
        final UUID projectId = uuidGeneratorPort.generate();
        this.projectStoragePort.createProject(projectId, command.getName(),
                command.getShortDescription(), command.getLongDescription(),
                command.getIsLookingForContributors(), command.getMoreInfos(),
                command.getGithubRepoIds(), command.getGithubUserIdsAsProjectLeads(),
                ProjectVisibility.PUBLIC,
                command.getImageUrl());
        return projectId;
    }

    @Override
    public URL saveLogoImage(InputStream imageInputStream) {
        return this.imageStoragePort.storeImage(imageInputStream);
    }
}
