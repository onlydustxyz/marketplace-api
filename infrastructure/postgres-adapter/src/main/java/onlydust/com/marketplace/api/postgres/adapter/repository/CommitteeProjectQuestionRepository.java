package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeProjectQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CommitteeProjectQuestionRepository extends JpaRepository<CommitteeProjectQuestionEntity, UUID> {

    @Modifying
    @Query(nativeQuery = true, value = """
                    delete from committee_project_questions
                    where committee_id = :committeeId
            """)
    void deleteAllByCommitteeId(UUID committeeId);
}
