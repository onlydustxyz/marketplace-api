package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.project.domain.model.Reward;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "reward_items", schema = "public")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@IdClass(RewardItemEntity.PrimaryKey.class)
public class RewardItemEntity {

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

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID rewardId;
        Long number;
        Long repoId;
    }

    public static List<RewardItemEntity> of(Reward reward) {
        return reward.rewardItems().stream().map(item -> RewardItemEntity.builder()
                .rewardId(reward.id().value())
                .number(item.number())
                .repoId(item.repoId())
                .id(item.id())
                .type(switch (item.type()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                })
                .projectId(reward.projectId().value())
                .recipientId(reward.recipientId())
                .build()).toList();
    }

    public Reward.Item toRewardItem() {
        return Reward.Item.builder()
                .id(id)
                .number(number)
                .repoId(repoId)
                .type(switch (type) {
                    case ISSUE -> Reward.Item.Type.ISSUE;
                    case PULL_REQUEST -> Reward.Item.Type.PULL_REQUEST;
                    case CODE_REVIEW -> Reward.Item.Type.CODE_REVIEW;
                }).build();
    }
}
