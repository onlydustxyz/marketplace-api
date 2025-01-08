package onlydust.com.marketplace.api.read.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.read.entities.project.ProjectRewardV2ReadEntity;

public interface ProjectRewardV2ReadRepository extends JpaRepository<ProjectRewardV2ReadEntity, UUID> {
    @Query(value = """
            select 
                r.reward_id                           as id,
                r.timestamp                           as requested_at,
                requestor.contributor                 as requestor,
                recipient.contributor                 as recipient,
                r.contribution_uuids                  as contributions,
                r.amount                              as amount,
                c.id                                  as currency_id,
                c.code                                as currency_code,
                c.name                                as currency_name,
                c.logo_url                            as currency_logo_url,
                c.decimals                            as currency_decimals,
                r.usd_amount                          as usd_amount
            from bi.p_reward_data r
                    join bi.p_contributor_global_data requestor on requestor.contributor_user_id = r.requestor_id
                    join bi.p_contributor_global_data recipient on recipient.contributor_id = r.contributor_id
                    join currencies c on c.id = r.currency_id
            where r.project_id = :projectId
                or r.project_slug = :projectSlug
            """, nativeQuery = true)
    Page<ProjectRewardV2ReadEntity> findAll(UUID projectId, String projectSlug, Pageable pageable);
} 