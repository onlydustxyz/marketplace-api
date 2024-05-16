package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Value
@Table(name = "reward_items", schema = "public")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode
@IdClass(RewardItemViewEntity.PrimaryKey.class)
@Immutable
public class RewardItemViewEntity {

    @Id
    @NonNull
    UUID rewardId;
    @Id
    @NonNull
    Long number;
    @Id
    @NonNull
    Long repoId;

    @NonNull
    String id;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "contribution_type")
    @NonNull
    ContributionType type;
    @NonNull
    UUID projectId;
    @NonNull
    Long recipientId;

    public enum ContributionType {
        ISSUE, PULL_REQUEST, CODE_REVIEW
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID rewardId;
        Long number;
        Long repoId;
    }
}
