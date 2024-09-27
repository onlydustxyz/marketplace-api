package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import org.apache.commons.lang3.tuple.Pair;
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
    @Autowired
    private ProjectCategoryStoragePort projectCategoryStoragePort;

    private final Faker faker = new Faker();

    public Pair<ProjectId, String> create(UserAuthHelper.AuthenticatedUser lead) {
        return create(lead, faker.funnyName().name(), List.of());
    }

    public Pair<ProjectId, String> create(UserAuthHelper.AuthenticatedUser lead, String name) {
        return create(lead, name, List.of());
    }

    public Pair<ProjectId, String> create(UserAuthHelper.AuthenticatedUser lead, String name, List<UUID> ecosystemIds) {
        return projectFacadePort.createProject(lead.userId(),
                CreateProjectCommand.builder()
                        .firstProjectLeaderId(lead.userId())
                        .name(name + " " + faker.random().nextLong())
                        .shortDescription(faker.lorem().sentence())
                        .longDescription(faker.lorem().paragraph())
                        .isLookingForContributors(faker.bool().bool())
                        .ecosystemIds(ecosystemIds)
                        .build());
    }


    public Project get(ProjectId projectId) {
        return projectStoragePort.getById(projectId)
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

    public ProjectCategory createCategory(String name) {
        final var category = ProjectCategory.of(name, faker.lorem().sentence(), faker.lorem().word());
        projectCategoryStoragePort.save(category);
        return category;
    }

    public void addCategory(ProjectId projectId, ProjectCategory.Id categoryId) {
        databaseHelper.executeQuery("""
                insert into projects_project_categories(project_id, project_category_id)
                values (:projectId, :categoryId);
                """, Map.of(
                "projectId", projectId.value(),
                "categoryId", categoryId.value()
        ));
    }
}
