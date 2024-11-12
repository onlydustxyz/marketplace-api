package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.project.ProjectAsContributorReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectsAsContributorRepository extends JpaRepository<ProjectAsContributorReadEntity, UUID> {

    @Query(value = """
            SELECT p.project_id                                                                                 as id,
                   p.project_slug                                                                               as slug,
                   p.project_name                                                                               as name,
                   p.project ->> 'shortDescription'                                                             as short_description,
                   p.project ->> 'logoUrl'                                                                      as logo_url,
                   p.project ->> 'visibility'                                                                   as visibility,
                   p.languages                                                                                  as languages,
                   p.leads                                                                                      as leads,
                   jsonb_agg(distinct jsonb_build_object(
                           'id', gr.id,
                           'owner', gr.owner_login,
                           'name', gr.name,
                           'description', gr.description,
                           'htmlUrl', gr.html_url
                                      )) filter ( where gr.id is not null )                                     as repos,
                   array_agg(distinct gfi.contribution_uuid) filter ( where gfi.contribution_uuid is not null ) as good_first_issue_ids,
                   coalesce(pcd.contributor_count, 0)                                                           as contributor_count,
                   count(distinct cd.contribution_uuid)                                                         as contribution_count,
                   coalesce(sum(r.usd_amount), 0)                                                               as rewarded_usd_amount,
                   p.categories                                                                                 as categories,
                   p.ecosystems                                                                                 as ecosystems,
                   pp.billing_profile_id                                                                        as billing_profile_id
            FROM bi.p_project_global_data p
                     JOIN iam.users u on u.github_user_id = :userGithubId
                     JOIN bi.p_project_contributions_data pcd on pcd.project_id = p.project_id
                     LEFT JOIN bi.p_per_contributor_contribution_data cd on cd.project_id = p.project_id and cd.contributor_id = u.github_user_id
                     LEFT JOIN bi.p_reward_data r on r.project_id = p.project_id and r.contributor_id = u.github_user_id
                     LEFT JOIN bi.p_contribution_data gfi on gfi.project_id = p.project_id and gfi.is_good_first_issue = true and gfi.activity_status = 'NOT_ASSIGNED'
                     LEFT JOIN indexer_exp.github_repos gr on gr.id = any (p.repo_ids)
                     LEFT JOIN accounting.payout_preferences pp on pp.project_id = p.project_id and pp.user_id = u.id
            
            WHERE p.project_visibility = 'PUBLIC'
            GROUP BY p.project_id, p.project_slug, p.project_name, p.project, p.languages, p.leads, pcd.contributor_count, p.categories, p.ecosystems, p.tags, pp.billing_profile_id
            HAVING count(cd.contribution_uuid) > 0
                OR sum(r.usd_amount) > 0
            """, nativeQuery = true)
    Page<ProjectAsContributorReadEntity> findAll(@NonNull Long userGithubId,
                                                 Pageable pageable);

}
