package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.bff.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "public_activity", schema = "public")
@Immutable
@Accessors(fluent = true)
@IdClass(ActivityReadEntity.PrimaryKey.class)
public class ActivityReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Enumerated(EnumType.STRING)
    PublicActivityType type;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Date timestamp;

    Long pullRequestAuthorId;
    UUID projectId;
    UUID rewardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    private ProjectReadEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rewardId", insertable = false, updatable = false)
    private RewardReadEntity reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pullRequestAuthorId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    private AllUserReadEntity pullRequestAuthor;

    public PublicActivityPageItemResponse toDto() {
        final var dto = new PublicActivityPageItemResponse()
                .timestamp(DateMapper.ofNullable(timestamp))
                .type(type);
        switch (type) {
            case PULL_REQUEST:
                dto.setPullRequest(new PublicActivityPageItemResponsePullRequest()
                        .project(project.toLinkResponse())
                        .author(pullRequestAuthor.toGithubUserResponse()));
                break;
            case REWARD_CREATED:
                dto.setRewardCreated(new PublicActivityPageItemResponseRewardCreated()
                        .project(project.toLinkResponse())
                        .recipient(reward.recipient().toGithubUserResponse())
                        .amount(new NewMoney(reward.amount(), reward.currency().toShortResponse())));
                break;
            case REWARD_CLAIMED:
                dto.setRewardClaimed(new PublicActivityPageItemResponseRewardCreated()
                        .project(project.toLinkResponse())
                        .recipient(reward.recipient().toGithubUserResponse())
                        .amount(new NewMoney(reward.amount(), reward.currency().toShortResponse())));
                break;
            case PROJECT_CREATED:
                dto.setProjectCreated(new PublicActivityPageItemResponseProjectCreated()
                        .project(project.toLinkResponse())
                        .createdBy(project.getLeads().stream().findFirst().map(l -> l.toGithubUserResponse()).orElse(null)));
                break;
        }
        return dto;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        PublicActivityType type;
        Date timestamp;
    }
}