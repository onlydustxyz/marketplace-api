package onlydust.com.marketplace.api.read.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import onlydust.com.marketplace.api.read.entities.github.GithubLabelWithCountReadEntity;

public interface GithubLabelWithCountReadRepository extends Repository<GithubLabelWithCountReadEntity, Long> {
    @Query(value = """
        select  gl.id                        as id,
                gl.name                      as name,
                gl.description               as description,
                count(distinct gil.issue_id) as issue_count
            from indexer_exp.github_labels gl
                    left join indexer_exp.github_issues_labels gil on gl.id = gil.label_id
            where issue_id in (select c.issue_id
                            from bi.p_contribution_data c
                                        join bi.p_contribution_contributors_data ccd on ccd.contribution_uuid = c.contribution_uuid
                            where (c.project_id = :projectId or c.project_slug = :projectSlug)
                                and c.contribution_type = 'ISSUE'
                                and c.contribution_status = 'IN_PROGRESS'
                                and coalesce(array_length(ccd.assignee_ids, 1), 0) = 0
                                and not exists (select 1
                                                from hackathons h
                                                        join hackathon_projects hp on hp.hackathon_id = h.id and hp.project_id = c.project_id
                                                        join indexer_exp.github_labels gl on gl.name = any (h.github_labels) and gl.id = any (c.github_label_ids)
                                                where h.status = 'PUBLISHED'
                                                and h.start_date > now())
                                and (coalesce(:githubLabels) is null or (
                                        select array_agg(name)
                                        from indexer_exp.github_labels 
                                        where id = any (c.github_label_ids)) @> cast(:githubLabels as text[])))
            group by gl.id, gl.name, gl.description
            order by issue_count desc, name
    """, nativeQuery = true)
    List<GithubLabelWithCountReadEntity> findForAvailableIssues(UUID projectId,
                                                                String projectSlug,
                                                                String[] githubLabels);
}
