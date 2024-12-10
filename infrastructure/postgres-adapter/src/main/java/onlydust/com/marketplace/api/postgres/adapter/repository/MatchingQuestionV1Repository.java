package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingQuestionV1Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchingQuestionV1Repository extends JpaRepository<MatchingQuestionV1Entity, UUID> {
    List<MatchingQuestionV1Entity> findAllByOrderByIndex();
}