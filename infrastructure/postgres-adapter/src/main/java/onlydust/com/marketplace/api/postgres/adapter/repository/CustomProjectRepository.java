package onlydust.com.marketplace.api.postgres.adapter.repository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Slf4j
public class CustomProjectRepository {

    protected static final String FIND_PROJECT_ECOSYSTEMS_QUERY = """
                select
                    e.*
                from ecosystems e
                join projects_ecosystems pe on pe.ecosystem_id = e.id and pe.project_id = :projectId
            """;

    private final EntityManager entityManager;

    public List<EcosystemEntity> getProjectEcosystems(UUID projectId) {
        return entityManager
                .createNativeQuery(FIND_PROJECT_ECOSYSTEMS_QUERY, EcosystemEntity.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public Boolean hasRemainingBudget(final UUID projectId) {
        final List remainingBudgets = entityManager.createNativeQuery("""
                        select 1
                        from project_allowances pa
                        where pa.current_allowance > 0
                        and pa.project_id = :projectId""")
                .setParameter("projectId", projectId)
                .getResultList();
        return isNull(remainingBudgets) || remainingBudgets.isEmpty() ? false : true;
    }

    public boolean isProjectPublic(UUID projectId) {
        final var isProjectPublic = entityManager.createNativeQuery("""
                        select 1
                        from projects p
                        where p.id = :projectId
                          and p.visibility = 'PUBLIC'
                        """)
                .setParameter("projectId", projectId)
                .getResultList();
        return !isProjectPublic.isEmpty();
    }

    public boolean isProjectPublic(String projectSlug) {
        final var isProjectPublic = entityManager.createNativeQuery("""
                        select 1
                        from projects p
                        where p.slug = :projectSlug
                          and p.visibility = 'PUBLIC'
                        """)
                .setParameter("projectSlug", projectSlug)
                .getResultList();
        return !isProjectPublic.isEmpty();
    }

    public boolean hasUserAccessToProject(UUID projectId, UUID userId) {
        final var hasAccessToProject = entityManager.createNativeQuery("""
                        select 1
                        from projects p
                        left join (select pl_me.project_id, case count(*) when 0 then false else true end is_lead
                                from project_leads pl_me
                                where pl_me.user_id = :userId
                                group by pl_me.project_id) is_me_lead on is_me_lead.project_id = p.id
                        left join (select ppc.project_id, case count(*) when 0 then false else true end is_p_c
                                from projects_pending_contributors ppc
                                         left join iam.users me on me.github_user_id = ppc.github_user_id
                                where me.id = :userId
                                group by ppc.project_id) is_pending_contributor on is_pending_contributor.project_id = p.id
                        left join (select ppli.project_id, case count(*) when 0 then false else true end is_p_pl
                                from pending_project_leader_invitations ppli
                                         left join iam.users me on me.github_user_id = ppli.github_user_id
                                where me.id = :userId
                                group by ppli.project_id) is_pending_pl on is_pending_pl.project_id = p.id
                        left join (select pl_count.project_id, count(pl_count.user_id) project_lead_count
                                from project_leads pl_count
                                group by pl_count.project_id) pl_count on pl_count.project_id = p.id
                        where p.id = :projectId
                            and
                            ((pl_count.project_lead_count > 0 or coalesce(is_pending_pl.is_p_pl, false))
                                and (coalesce(is_pending_pl.is_p_pl, false) or
                                     coalesce(is_me_lead.is_lead, false) or
                                     coalesce(is_pending_contributor.is_p_c, false)))
                        """)
                .setParameter("projectId", projectId)
                .setParameter("userId", userId)
                .getResultList();
        return !hasAccessToProject.isEmpty();
    }

    public boolean hasUserAccessToProject(String projectSlug, UUID userId) {
        final var hasAccessToProject = entityManager.createNativeQuery("""
                        select 1
                        from projects p
                        left join (select pl_me.project_id, case count(*) when 0 then false else true end is_lead
                                from project_leads pl_me
                                where pl_me.user_id = :userId
                                group by pl_me.project_id) is_me_lead on is_me_lead.project_id = p.id
                        left join (select ppc.project_id, case count(*) when 0 then false else true end is_p_c
                                from projects_pending_contributors ppc
                                         left join iam.users me on me.github_user_id = ppc.github_user_id
                                where me.id = :userId
                                group by ppc.project_id) is_pending_contributor on is_pending_contributor.project_id = p.id
                        left join (select ppli.project_id, case count(*) when 0 then false else true end is_p_pl
                                from pending_project_leader_invitations ppli
                                         left join iam.users me on me.github_user_id = ppli.github_user_id
                                where me.id = :userId
                                group by ppli.project_id) is_pending_pl on is_pending_pl.project_id = p.id
                        left join (select pl_count.project_id, count(pl_count.user_id) project_lead_count
                                from project_leads pl_count
                                group by pl_count.project_id) pl_count on pl_count.project_id = p.id
                        where p.slug = :projectSlug
                            and
                            ((pl_count.project_lead_count > 0 or coalesce(is_pending_pl.is_p_pl, false))
                                and (coalesce(is_pending_pl.is_p_pl, false) or
                                     coalesce(is_me_lead.is_lead, false) or
                                     coalesce(is_pending_contributor.is_p_c, false)))
                        """)
                .setParameter("projectSlug", projectSlug)
                .setParameter("userId", userId)
                .getResultList();
        return !hasAccessToProject.isEmpty();
    }
}
