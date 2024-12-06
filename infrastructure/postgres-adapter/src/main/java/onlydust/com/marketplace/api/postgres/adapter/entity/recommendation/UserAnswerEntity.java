package onlydust.com.marketplace.api.postgres.adapter.entity.recommendation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_answers", schema = "reco")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserAnswerEntity.PrimaryKey.class)
public class UserAnswerEntity {
    @Id
    private UUID userId;
    @Id
    private UUID questionId;
    @Id
    private UUID answerId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private UUID userId;
        private UUID questionId;
        private UUID answerId;
    }
} 