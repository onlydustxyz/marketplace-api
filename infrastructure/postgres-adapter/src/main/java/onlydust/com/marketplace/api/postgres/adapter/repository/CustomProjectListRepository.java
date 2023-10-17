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
import javax.persistence.Query;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class CustomProjectListRepository {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static TypeReference<HashMap<String, Integer>> typeRef
            = new TypeReference<>() {
    };
    private final EntityManager entityManager;

    protected static final String FIND_PROJECTS_BASE_QUERY = """
            select row_number() over (%order_by%), search_project.*
            from (select p.project_id,
                         p.hiring,
                         p.logo_url,
                         p.key,
                         p.name,
                         p.short_description,
                         p.long_description,
                         p.visibility,
                         p.rank,
                         cast(gr.languages as text),
                         gr.id                                         repository_id,
                         u.id                                          p_lead_id,
                         u.login_at_signup                             p_lead_login,
                         u.avatar_url_at_signup                        p_lead_avatar_url,
                         s.name                                        sponsor_name,
                         s.logo_url                                    sponsor_logo_url,
                         s.id                                          sponsor_id,
                         (select count(github_repo_id)
                          from project_github_repos pgr_count
                          where pgr_count.project_id = p.project_id)   repo_count,
                         (select count(github_user_id) contributors_count
                          from projects_contributors pc_count
                          where pc_count.project_id = p.project_id)    contributors_count,
                         (select count(pl_count.user_id)
                          from project_leads pl_count
                          where pl_count.project_id = p.project_id) as project_lead_count,
                          false is_pending_project_lead
                  from project_details p
                           join projects_budgets pb on pb.project_id = p.project_id
                           left join projects_sponsors ps on ps.project_id = p.project_id
                           left join sponsors s on s.id = ps.sponsor_id
                           left join project_github_repos pgr on pgr.project_id = p.project_id
                           left join github_repos gr on gr.id = pgr.github_repo_id
                           left join project_leads pl on pl.project_id = p.project_id
                           left join auth_users u on u.id = pl.user_id) as search_project
            where repo_count > 0
              and search_project.visibility = 'PUBLIC'
              and project_lead_count > 0 
            """;

    protected static final String FIND_PROJECTS_FOR_USER_BASE_QUERY = """
            select row_number() over (%order_by%), search_project.*
            from (select p.project_id,
                         p.hiring,
                         p.logo_url,
                         p.key,
                         p.name,
                         p.short_description,
                         p.long_description,
                         p.visibility,
                         p.rank,
                         cast(gr.languages as text),
                         gr.id                                                          repository_id,
                         u.id                                                           p_lead_id,
                         u.login_at_signup                                              p_lead_login,
                         u.avatar_url_at_signup                                         p_lead_avatar_url,
                         s.name                                                         sponsor_name,
                         s.logo_url                                                     sponsor_logo_url,
                         s.id                                                           sponsor_id,
                         (select count(github_repo_id)
                          from project_github_repos pgr_count
                          where pgr_count.project_id = p.project_id)                    repo_count,
                         (select count(github_user_id) contributors_count
                          from projects_contributors pc_count
                          where pc_count.project_id = p.project_id)                     contributors_count,
                         (select count(pl_count.user_id)
                          from project_leads pl_count
                          where pl_count.project_id = p.project_id) as                  project_lead_count,
                         (select case count(*) when 0 then false else true end
                          from project_leads pl_me
                          where pl_me.project_id = p.project_id
                            and pl_me.user_id = :userId ) is_lead,
                         (select case count(*) when 0 then false else true end
                          from projects_contributors pc_me
                                   left join auth_users me on me.github_user_id = pc_me.github_user_id
                          where pc_me.project_id = p.project_id
                            and me.id = :userId )         is_contributor,
                         (select case count(*) when 0 then false else true end
                          from projects_pending_contributors ppc
                                   left join auth_users me on me.github_user_id = ppc.github_user_id
                          where ppc.project_id = p.project_id
                            and me.id = :userId )         is_pending_contributor,
                         (select case count(*) when 0 then false else true end
                          from pending_project_leader_invitations ppli
                                   left join auth_users me on me.github_user_id = ppli.github_user_id
                          where ppli.project_id = p.project_id
                            and me.id = :userId )         is_pending_project_lead,
                         (select case count(*) when 0 then false else true end
                          from projects_rewarded_users pru
                                   left join auth_users me on me.github_user_id = pru.github_user_id
                          where pru.project_id = p.project_id
                            and me.id = :userId )         was_rewarded
                  from project_details p
                           join projects_budgets pb on pb.project_id = p.project_id
                           left join projects_sponsors ps on ps.project_id = p.project_id
                           left join sponsors s on s.id = ps.sponsor_id
                           left join project_github_repos pgr on pgr.project_id = p.project_id
                           left join github_repos gr on gr.id = pgr.github_repo_id
                           left join project_leads pl on pl.project_id = p.project_id
                           left join auth_users u on u.id = pl.user_id) as search_project
            where repo_count > 0
              and ((search_project.visibility = 'PUBLIC' and (project_lead_count > 0 or is_pending_project_lead))
                or (search_project.visibility = 'PRIVATE' and
                    (is_contributor or is_pending_project_lead or is_lead or is_pending_contributor))) 
            """;


    public Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies,
                                                                              List<String> sponsors,
                                                                              String search,
                                                                              ProjectCardView.SortBy sort,
                                                                              UUID userId, Boolean mine) {
        final String query = buildQueryForUser(technologies, sponsors, search, sort, mine);
        Query nativeQuery = entityManager.createNativeQuery(query, ProjectViewEntity.class)
                .setParameter("userId", userId);
        if (nonNull(search) && !search.isEmpty()) {
            nativeQuery = nativeQuery.setParameter("search", search);
        }
        return executeQueryAndMapResults(nativeQuery);
    }


    public Page<ProjectCardView> findByTechnologiesSponsorsSearchSortBy(List<String> technologies, List<String> sponsors
            , String search, ProjectCardView.SortBy sort) {
        final String query = buildQuery(technologies, sponsors, search, sort);
        Query nativeQuery = entityManager.createNativeQuery(query, ProjectViewEntity.class);
        if (nonNull(search) && !search.isEmpty()) {
            nativeQuery = nativeQuery.setParameter("search", search);
        }
        return executeQueryAndMapResults(nativeQuery);
    }

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
                    .isInvitedAsProjectLead(entity.getIsPendingProjectLead())
                    .build();
            projectViewMap.put(entity.getId(), projectCardView);
        }
    }

    private static void addRepoTechnologiesToProject(ProjectViewEntity entity, ProjectCardView projectCardView) {
        try {
            if (isNull(entity.getRepositoryLanguages())) {
                return;
            }
            final HashMap<String, Integer> technologies =
                    objectMapper.readValue(entity.getRepositoryLanguages(), typeRef);
            projectCardView.addTechnologies(technologies);
        } catch (JsonProcessingException e) {
            LOGGER.warn("No technologies found", e);
        }
    }


    private static void addSponsorToProject(ProjectViewEntity entity, ProjectCardView projectCardView) {
        if (nonNull(entity.getSponsorId())) {
            final SponsorView sponsorView = SponsorView.builder()
                    .logoUrl(entity.getSponsorLogoUrl())
                    .name(entity.getSponsorName())
                    .id(entity.getSponsorId())
                    .build();
            projectCardView.addSponsor(sponsorView);
        }
    }

    private static void addProjectLeadToProject(ProjectViewEntity entity, ProjectCardView projectCardView) {
        final ProjectLeaderLinkView projectLeaderLinkView = ProjectLeaderLinkView.builder()
                .avatarUrl(entity.getProjectLeadAvatarUrl())
                .id(entity.getProjectLeadId())
                .login(entity.getProjectLeadLogin())
                .build();
        projectCardView.addProjectLeader(projectLeaderLinkView);
    }

    protected static String buildQueryForUser(final List<String> technologies,
                                              final List<String> sponsors,
                                              final String search,
                                              final ProjectCardView.SortBy sort,
                                              final Boolean mine) {
        final List<String> whereConditions = new ArrayList<>();
        Optional<String> orderByCondition = Optional.empty();
        if (nonNull(search) && !search.isEmpty()) {
            whereConditions.add(
                    "search_project.short_description like " + getSearchSanitizedSqlValue() + " or search_project" +
                    ".name like " + getSearchSanitizedSqlValue());
        }
        if (mine) {
            whereConditions.add("search_project.pl_user_id = :userId");
        }
        if (nonNull(sort)) {
            switch (sort) {
                case CONTRIBUTORS_COUNT ->
                        orderByCondition = Optional.of("order by search_project.contributors_count desc");
                case NAME -> orderByCondition = Optional.of("order by search_project.name");
                case RANK -> orderByCondition = Optional.of("order by search_project.rank desc");
                case REPOS_COUNT -> orderByCondition = Optional.of("order by search_project.repo_count desc");
            }
        }
        if (nonNull(sponsors) && !sponsors.isEmpty()) {
            whereConditions.add("search_project.sponsor_name in (" + String.join(",", sponsors.stream().map(s ->
                    "'" + s + "'").toList()) + ")");
        }
        if (nonNull(technologies) && !technologies.isEmpty()) {
            whereConditions.add("search_project.languages like " + String.join(" or search_project" +
                                                                               ".languages like ",
                    technologies.stream().map(s -> "'%\"" + s + "\"%'").toList()));
        }
        return FIND_PROJECTS_FOR_USER_BASE_QUERY.replace("%order_by%", orderByCondition.orElse("order by " +
                                                                                               "search_project" +
                                                                                               ".project_id")) + (whereConditions.isEmpty() ? "" : " and " + String.join(" and ",
                whereConditions.stream().map(s -> "(" + s + ")").toList()));
    }

    private static String getSearchSanitizedSqlValue() {
        return "CONCAT('%', :search, '%')";
    }

    protected static String buildQuery(final List<String> technologies,
                                       final List<String> sponsors,
                                       final String search, final ProjectCardView.SortBy sort) {
        final List<String> whereConditions = new ArrayList<>();
        Optional<String> orderByCondition = Optional.empty();
        if (nonNull(search) && !search.isEmpty()) {
            whereConditions.add(
                    "search_project.short_description like " + getSearchSanitizedSqlValue() + " or search_project" +
                    ".name like " + getSearchSanitizedSqlValue());
        }
        if (nonNull(sort)) {
            switch (sort) {
                case CONTRIBUTORS_COUNT ->
                        orderByCondition = Optional.of("order by search_project.contributors_count desc");
                case NAME -> orderByCondition = Optional.of("order by search_project.name");
                case RANK -> orderByCondition = Optional.of("order by search_project.rank desc");
                case REPOS_COUNT -> orderByCondition = Optional.of("order by search_project.repo_count desc");
            }
        }
        if (nonNull(sponsors) && !sponsors.isEmpty()) {
            whereConditions.add("search_project.sponsor_name in (" + String.join(",", sponsors.stream().map(s ->
                    "'" + s + "'").toList()) + ")");
        }
        if (nonNull(technologies) && !technologies.isEmpty()) {
            whereConditions.add("search_project.languages like " + String.join(" or search_project" +
                                                                               ".languages like ",
                    technologies.stream().map(s -> "'%\"" + s + "\"%'").toList()));
        }
        return FIND_PROJECTS_BASE_QUERY.replace("%order_by%", orderByCondition.orElse("order by search_project" +
                                                                                      ".project_id")) + (whereConditions.isEmpty() ? "" : " and " + String.join(" and ",
                whereConditions.stream().map(s -> "(" + s + ")").toList()));
    }

    private Page<ProjectCardView> executeQueryAndMapResults(Query nativeQuery) {
        final List<ProjectViewEntity> rows = nativeQuery.getResultList();
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
