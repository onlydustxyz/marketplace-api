package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {
    Optional<ApplicationEntity> findByApplicantIdAndProjectIdAndIssueId(Long applicantId, UUID projectId, Long issueId);

    List<ApplicationEntity> findAllByApplicantIdAndIssueId(Long applicantId, Long issueId);

    List<ApplicationEntity> findAllByCommentId(Long commentId);

    List<ApplicationEntity> findAllByIssueId(Long issueId);

    void deleteAllByIssueId(Long issueId);

    @Modifying
    @Query(value = """
            DELETE
            FROM applications a
            WHERE a.received_at < now() - interval '1 month'
              AND NOT EXISTS(SELECT 1
                             FROM indexer_exp.github_issues gi
                                      LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = gi.id
                             WHERE gi.id = a.issue_id
                               AND gi.status = 'OPEN'
                               AND gia.user_id IS NULL)
            """, nativeQuery = true)
    void deleteObsoleteApplications();

    List<ApplicationEntity> findAllByProjectIdAndIssueId(UUID projectId, Long issueId);
}
