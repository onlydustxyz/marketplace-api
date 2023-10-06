package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.api.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;

import javax.persistence.EntityManager;
import java.util.*;

import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class CustomProjectRepository {

    protected static final String FIND_PROJECTS_BASE_QUERY = "select row_number() over (order by " +
            "search_project.project_id), search_project.* from (" +
            "select p.project_id," +
            "       p.hiring," +
            "       p.logo_url," +
            "       p.key," +
            "       p.name," +
            "       p.short_description," +
            "       p.visibility," +
            "       p.rank," +
            "       cast(gr.languages as text)," +
            "       gr.id repository_id," +
            "       u.id                   p_lead_id," +
            "       u.login_at_signup      p_lead_login," +
            "       u.avatar_url_at_signup p_lead_avatar_url," +
            "       s.name sponsor_name," +
            "       s.logo_url sponsor_logo_url," +
            "       s.id sponsor_id," +
            "       repo_count.repo_count," +
            "       coalesce(contributors_count.contributors_count,0) contributors_count" +
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
            "p.project_id" +
            ") as search_project";
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static TypeReference<HashMap<String, Integer>> typeRef
            = new TypeReference<>() {
    };
    private final EntityManager entityManager;

    private static void entityToProjectView(ProjectViewEntity entity, Map<UUID, ProjectCardView> projectViewMap) {
        if (!projectViewMap.containsKey(entity.getId())) {
            final ProjectCardView projectCardView = ProjectCardView.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .logoUrl(entity.getLogoUrl())
                    .hiring(entity.getHiring())
                    .visibility(ProjectVisibility.valueOf(entity.getVisibility().name()))
                    .slug(entity.getKey())
                    .shortDescription(entity.getShortDescription())
                    .contributorCount(entity.getContributorsCount())
                    .repoCount(entity.getRepoCount())
                    .build();
            projectViewMap.put(entity.getId(), projectCardView);
        }
    }

    private static void addRepoTechnologiesToProject(ProjectViewEntity entity, ProjectCardView projectCardView) {
        try {
            final HashMap<String, Integer> technologies =
                    objectMapper.readValue(entity.getRepositoryLanguages(), typeRef);
            projectCardView.addTechnologies(technologies);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private static void addSponsorToProject(ProjectViewEntity entity, ProjectCardView projectCardView) {
        final SponsorView sponsorView = SponsorView.builder()
                .logoUrl(entity.getSponsorLogoUrl())
                .name(entity.getSponsorName())
                .id(entity.getSponsorId())
                .build();
        projectCardView.addSponsor(sponsorView);
    }

    private static void addProjectLeadToProject(ProjectViewEntity entity, ProjectCardView projectCardView) {
        final ProjectLeaderLinkView projectLeaderLinkView = ProjectLeaderLinkView.builder()
                .avatarUrl(entity.getProjectLeadAvatarUrl())
                .id(entity.getProjectLeadId())
                .login(entity.getProjectLeadLogin())
                .build();
        projectCardView.addProjectLeader(projectLeaderLinkView);
    }

    protected static String buildQuery(final List<String> technologies,
                                       final List<String> sponsors, final UUID userId,
                                       final String search, final ProjectCardView.SortBy sort) {
        final List<String> whereConditions = new ArrayList<>();
        Optional<String> orderByCondition = Optional.empty();
        if (nonNull(search) && !search.isEmpty()) {
            whereConditions.add(
                    "search_project.short_description like '%" + search + "%' or search_project.name like '%" + search +
                            "%'");
        }
        if (nonNull(sort)) {
            switch (sort) {
                case CONTRIBUTORS_COUNT ->
                        orderByCondition = Optional.of(" order by search_project.contributors_count desc");
                case NAME -> orderByCondition = Optional.of(" order by search_project.name asc");
                case RANK -> orderByCondition = Optional.of(" order by search_project.rank desc");
                case REPOS_COUNT -> orderByCondition = Optional.of(" order by search_project.repo_count desc");
            }
        }
        if (nonNull(userId)) {
            whereConditions.add("search_project.pl_user_id = '" + userId + "'");
        }
        if (nonNull(sponsors) && !sponsors.isEmpty()) {
            whereConditions.add("search_project.sponsor_name in (" + String.join(",", sponsors.stream().map(s ->
                    "'" + s + "'").toList()) + ")");
        }
        if (nonNull(technologies) && !technologies.isEmpty()) {
            whereConditions.add("search_project.languages like " + String.join(" or search_project" +
                    ".languages like ", technologies.stream().map(s -> "'%\"" + s + "\"%'").toList()));
        }
        return FIND_PROJECTS_BASE_QUERY + (whereConditions.isEmpty() ? "" : " where " + String.join(" and ",
                whereConditions.stream().map(s -> "(" + s + ")").toList())) + orderByCondition.orElse("");
    }

    public Page<ProjectCardView> findByTechnologiesSponsorsOwnershipSearchSortBy(final List<String> technologies,
                                                                                 final List<String> sponsor,
                                                                                 final UUID userId,
                                                                                 final String search,
                                                                                 final ProjectCardView.SortBy sort) {
        final String query = buildQuery(technologies, sponsor, userId, search, sort);
        LOGGER.debug(query);
        final List<ProjectViewEntity> rows =
                entityManager.createNativeQuery(query, ProjectViewEntity.class).getResultList();
        final Map<UUID, ProjectCardView> projectViewMap = new LinkedHashMap<>();
        for (ProjectViewEntity entity : rows) {
            entityToProjectView(entity, projectViewMap);
            final ProjectCardView projectCardView = projectViewMap.get(entity.getId());
            addProjectLeadToProject(entity, projectCardView);
            addSponsorToProject(entity, projectCardView);
            addRepoTechnologiesToProject(entity, projectCardView);
        }
        return Page.<ProjectCardView>builder()
                .content(projectViewMap.values().stream().toList())
                .build();
    }
}
