package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HackathonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface HackathonRepository extends JpaRepository<HackathonEntity, UUID> {

    @Query(value = """
            SELECT h.*
            FROM hackathons h
                    JOIN hackathon_issues hi ON hi.hackathon_id = h.id AND hi.issue_id = :issueId
            WHERE h.status = 'PUBLISHED'
              AND h.end_date > now()
            ORDER BY h.start_date ASC NULLS LAST, h.end_date ASC NULLS LAST
            LIMIT 1;
            """, nativeQuery = true
    )
    Optional<HackathonEntity> findUpcomingHackathonByIssueId(Long issueId);
}
