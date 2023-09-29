package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.view.ProjectViewEntity;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class CustomProjectRepository {

    private static final String FIND_PROJECTS_BASE_QUERY = "select * from (" +
            "select p.project_id," +
            "       p.hiring," +
            "       p.logo_url," +
            "       p.key," +
            "       p.name," +
            "       p.short_description," +
            "       p.visibility," +
            "       cast(gr.languages as text)," +
            "       gr.id repository_id," +
            "       u.id                   p_lead_id," +
            "       u.login_at_signup      p_lead_login," +
            "       u.avatar_url_at_signup p_lead_avatar_url," +
            "       s.name sponsor_name," +
            "       s.logo_url sponsor_logo_url," +
            "       s.id sponsor_id," +
            "       repo_count.repo_count," +
            "       contributors_count.contributors_count" +
            " from project_details p" +
            "         left join projects_sponsors ps on ps.project_id = p.project_id" +
            "         left join sponsors s on s.id = ps.sponsor_id" +
            "         left join project_github_repos pgr on pgr.project_id = p.project_id" +
            "         left join github_repos gr on gr.id = pgr.github_repo_id" +
            "         left join project_leads pl on pl.project_id = p.project_id" +
            "         left join auth_users u on u.id = pl.user_id" +
            "         left join (select count(github_repo_id) repo_count, project_id" +
            "                    from project_github_repos" +
            "                    group by project_id) as repo_count on repo_count.project_id = p.project_id" +
            "        left join (select count(github_user_id) contributors_count, project_id" +
            "                   from projects_contributors" +
            "                   group by project_id) as contributors_count on contributors_count.project_id = " +
            "p.project_id"+
            ") as search_project order by search_project.project_id";


    private final EntityManager entityManager;
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static TypeReference<HashMap<String, Integer>> typeRef
            = new TypeReference<>() {
    };

    public Page<ProjectView> findByTechnologiesSponsorsOwnershipSearchSortBy(List<String> technology,
                                                                             List<String> sponsor, String ownership,
                                                                             String search, String sort) {

        final List<ProjectViewEntity> rows = entityManager.createNativeQuery(FIND_PROJECTS_BASE_QUERY,
                ProjectViewEntity.class).getResultList();
        final Map<UUID, ProjectView> projectViewMap = new HashMap<>();
        for (ProjectViewEntity entity : rows) {
            if (!projectViewMap.containsKey(entity.getId())) {
                final ProjectView projectView = ProjectView.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .logoUrl(entity.getLogoUrl())
                        .hiring(entity.getHiring())
                        .slug(entity.getKey())
                        .shortDescription(entity.getShortDescription())
                        .contributorCount(entity.getContributorsCount())
                        .repositoryCount(entity.getRepoCount())
                        .build();
                projectViewMap.put(entity.getId(), projectView);
            }
            final ProjectView projectView = projectViewMap.get(entity.getId());
            addProjectLeadToProject(entity, projectView);
            addSponsorToProject(entity, projectView);
            addRepositoryToProject(entity, projectView);
        }

        return Page.<ProjectView>builder()
                .content(projectViewMap.values().stream().toList())
                .build();
    }

    private static void addRepositoryToProject(ProjectViewEntity entity, ProjectView projectView) {
        try {
            final HashMap<String, Integer> technologies =
                    objectMapper.readValue(entity.getRepositoryLanguages(), typeRef);
            final RepositoryView repositoryView = RepositoryView.builder()
                    .id(entity.getRepositoryId())
                    .technologies(technologies)
                    .build();
            projectView.addRepository(repositoryView);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addSponsorToProject(ProjectViewEntity entity, ProjectView projectView) {
        final SponsorView sponsorView = SponsorView.builder()
                .logoUrl(entity.getSponsorLogoUrl())
                .name(entity.getSponsorName())
                .id(entity.getSponsorId())
                .build();
        projectView.addSponsor(sponsorView);
    }

    private static void addProjectLeadToProject(ProjectViewEntity entity, ProjectView projectView) {
        final ProjectLeadView projectLeadView = ProjectLeadView.builder()
                .avatarUrl(entity.getProjectLeadAvatarUrl())
                .id(entity.getProjectLeadId())
                .login(entity.getProjectLeadLogin())
                .build();
        projectView.addProjectLead(projectLeadView);
    }
}
