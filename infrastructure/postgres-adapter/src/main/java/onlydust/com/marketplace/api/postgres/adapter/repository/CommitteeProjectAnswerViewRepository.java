package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeProjectAnswerViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommitteeProjectAnswerViewRepository extends JpaRepository<CommitteeProjectAnswerViewEntity, CommitteeProjectAnswerViewEntity.PrimaryKey> {

    @Query(value = """
            select
                a
            from
                CommitteeProjectAnswerViewEntity a
                join fetch a.projectQuestion
            where
                a.committeeId = :committeeId
                and a.projectId = :projectId
            order by a.projectQuestion.rank
            """)
    List<CommitteeProjectAnswerViewEntity> findByCommitteeIdAndAndProjectId(UUID committeeId, UUID projectId);
}
