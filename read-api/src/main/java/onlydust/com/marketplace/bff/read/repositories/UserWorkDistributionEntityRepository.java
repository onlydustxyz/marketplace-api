package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserWorkDistributionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserWorkDistributionEntityRepository extends Repository<UserWorkDistributionEntity, String> {
    @Query(value = """
            SELECT
                c.contributor_id                                     as contributor_id,
                count(c.id) FILTER ( WHERE c.type = 'CODE_REVIEW' )  as code_review_count,
                count(c.id) FILTER ( WHERE c.type = 'ISSUE' )        as issue_count,
                count(c.id) FILTER ( WHERE c.type = 'PULL_REQUEST' ) as pull_request_count
            FROM
                indexer_exp.contributions c
                JOIN indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
                JOIN project_github_repos pgr on pgr.github_repo_id = c.repo_id
                JOIN projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
            WHERE
                c.contributor_id = :githubUserId
            GROUP BY
                c.contributor_id
            """, nativeQuery = true)
    Optional<UserWorkDistributionEntity> findByContributorId(Long githubUserId);
}
