package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserProfileLanguagePageItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface UserProfileLanguagePageItemEntityRepository extends Repository<UserProfileLanguagePageItemEntity, UUID> {
    @Query(value = """
            select lfe.language_id                    as language_id,
                   ulr.rank                           as rank,
                   'GREEN'                            as contributing_status,
                   count(distinct c.id)               as contribution_count,
                   count(distinct ri.reward_id)       as reward_count,
                   ROUND(sum(rewarded.usd_amount), 2) as total_earned_usd,
                   jsonb_agg(distinct jsonb_build_object(
                           'id', p.id,
                           'slug', p.key,
                           'name', p.name,
                           'logoUrl', p.logo_url
                             ))                       as projects
            from indexer_exp.contributions c
                     join language_file_extensions lfe
                          on lfe.extension = any (c.main_file_extensions)
                     join users_languages_ranks ulr on ulr.language_id = lfe.language_id and ulr.contributor_id = c.contributor_id
                     join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                     join reward_items ri
                          on ri.id = coalesce(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id) and
                             ri.recipient_id = c.contributor_id
                     join projects p on p.id = pgr.project_id
                     join lateral ( select distinct on (reward_id) amount_usd_equivalent as usd_amount
                                    from accounting.reward_status_data rsd
                                    where rsd.reward_id = ri.reward_id) rewarded on true
            where c.contributor_id = :githubUserId
            group by lfe.language_id, ulr.rank
            """, nativeQuery = true)
    Page<UserProfileLanguagePageItemEntity> findByContributorId(Long githubUserId, Pageable pageable);
}
