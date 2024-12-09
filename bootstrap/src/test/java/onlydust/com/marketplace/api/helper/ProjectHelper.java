package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.*;
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
    @Autowired
    private PostgresBiProjectorAdapter postgresBiProjectorAdapter;

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

    public void addEcosystem(ProjectId projectId, EcosystemId ecosystemId) {
        databaseHelper.executeQuery("""
                insert into projects_ecosystems(project_id, ecosystem_id)
                values (:projectId, :ecosystemId);
                """, Map.of(
                "projectId", projectId.value(),
                "ecosystemId", ecosystemId.value()
        ));
    }

    public void addLanguages(ProjectId projectId, List<Language.Id> languageIds) {
        final Long githubRepoId = databaseHelper.executeReadQuery(
                """
                                select github_repo_id
                                from project_github_repos
                                where project_id = :projectId
                                limit 1
                        """, Map.of("projectId", projectId.value())
        );
        final List<String> languageExtensions = databaseHelper.executeReadListQuery("""
                select extension from language_file_extensions where cast(language_id as text) in :languageIds
                """, Map.of("languageIds", languageIds.stream().map(id -> id.value().toString()).toList()));
        databaseHelper.executeQuery("""
                insert into indexer_exp.github_pull_requests
                select id +1 ,
                       :repoId,
                       number,
                       title,
                       status,
                       created_at,
                       closed_at,
                       merged_at,
                       author_id,
                       html_url,
                       body,
                       comments_count,
                       tech_created_at,
                       tech_updated_at,
                       draft,
                       repo_owner_login,
                       repo_name,
                       repo_html_url,
                       author_login,
                       author_html_url,
                       author_avatar_url,
                       review_state,
                       commit_count,
                       array[:languageExtensions],
                       updated_at,
                       indexer.uuid_of(cast(id + 1 as text))
                from indexer_exp.github_pull_requests order by id desc limit 1
                """, Map.of("repoId", githubRepoId, "languageExtensions", String.join(",", languageExtensions)));
        databaseHelper.executeQuery("""
                    REFRESH MATERIALIZED VIEW project_languages
                """, Map.of());
    }

    public void inviteProjectLead(ProjectId projectId, UserAuthHelper.AuthenticatedUser lead) {
        databaseHelper.executeQuery("""
                insert into pending_project_leader_invitations(id, project_id, github_user_id)
                select gen_random_uuid(), :projectId, :githubUserId
                """, Map.of(
                "projectId", projectId.value(),
                "githubUserId", lead.githubUserId().value()
        ));
        postgresBiProjectorAdapter.onProjectCreated(projectId, lead.userId());
    }
}
