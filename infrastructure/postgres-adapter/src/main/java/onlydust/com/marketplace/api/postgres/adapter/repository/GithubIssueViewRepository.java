package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubIssueViewEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface GithubIssueViewRepository extends Repository<GithubIssueViewEntity, Long> {
    Optional<GithubIssueViewEntity> findById(Long id);

    @Query(value = """
            select distinct gi.*
            from indexer_exp.github_labels gl
                     join indexer_exp.github_issues_labels gil on gl.id = gil.label_id
                     join indexer_exp.github_issues gi on gil.issue_id = gi.id
                     left join indexer_exp.github_issues_assignees gia on gi.id = gia.issue_id
            where gl.name ilike '%good%first%issue%'
              and (gil.tech_created_at >= :now at time zone :timezone - interval '5 minutes' or gi.created_at >= :now at time zone :timezone - interval '5 minutes')
              and gi.status = 'OPEN'
              and gia.user_id is null
              and NOT exists(SELECT 1 FROM hackathon_issues hi WHERE hi.issue_id = gi.id)
            """, nativeQuery = true)
    List<GithubIssueViewEntity> findAllGoodFirstIssuesCreatedSince5Minutes(ZonedDateTime now, String timezone);

}
