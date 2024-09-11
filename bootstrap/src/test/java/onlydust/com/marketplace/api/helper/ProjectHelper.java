package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectHelper {
    @Autowired
    private ProjectFacadePort projectFacadePort;
    @Autowired
    private ProjectStoragePort projectStoragePort;
    @Autowired
    private DatabaseHelper databaseHelper;

    private final Faker faker = new Faker();

    public ProjectId create(UserAuthHelper.AuthenticatedUser lead) {
        return ProjectId.of(projectFacadePort.createProject(lead.user().getId(),
                        CreateProjectCommand.builder()
                                .firstProjectLeaderId(lead.user().getId())
                                .name(faker.funnyName().name() + " " + faker.random().nextLong())
                                .shortDescription(faker.lorem().sentence())
                                .longDescription(faker.lorem().paragraph())
                                .isLookingForContributors(faker.bool().bool())
                                .build())
                .getLeft());
    }

    public ProjectId create(UserAuthHelper.AuthenticatedUser lead, String name) {
        return create(lead, name, List.of());
    }

    public ProjectId create(UserAuthHelper.AuthenticatedUser lead, String name, List<UUID> ecosystemIds) {
        return ProjectId.of(projectFacadePort.createProject(lead.user().getId(),
                        CreateProjectCommand.builder()
                                .firstProjectLeaderId(lead.user().getId())
                                .name(name + " " + faker.random().nextLong())
                                .shortDescription(faker.lorem().sentence())
                                .longDescription(faker.lorem().paragraph())
                                .isLookingForContributors(faker.bool().bool())
                                .ecosystemIds(ecosystemIds)
                                .build())
                .getLeft());
    }


    public Project get(ProjectId projectId) {
        return projectStoragePort.getById(projectId.value())
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public void addRepo(ProjectId projectId, Long repoId) {
        databaseHelper.executeQuery("""
                insert into project_github_repos(project_id, github_repo_id)
                values (:projectId, :repoId);
                """, Map.of(
                "projectId", projectId.value(),
                "repoId", repoId
        ));
    }

    public void updateVisibility(ProjectId projectId, ProjectVisibility projectVisibility) {
        databaseHelper.executeQuery("""
                        update projects
                        set visibility = cast(:visibility as project_visibility)
                        where id = :projectId
                """, Map.of("projectId", projectId.value(), "visibility", projectVisibility.name()));
    }
}
