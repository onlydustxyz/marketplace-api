package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<MatchingQuestionEntity, UUID> {
    List<MatchingQuestionEntity> findAllByMatchingSystemId(String matchingSystemId);

} 