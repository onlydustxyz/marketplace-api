package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.UserAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswerEntity, UserAnswerEntity.PrimaryKey> {
    List<UserAnswerEntity> findAllByUserIdAndQuestionIdIn(UUID userId, Collection<UUID> questionIds);

    @Modifying
    void deleteAllByUserIdAndQuestionId(UUID userId, UUID questionId);
} 