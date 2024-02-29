package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true)
@Entity
public class RewardViewEntity {
    @Id
    UUID id;
    Date requestedAt;
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    @NonNull ProjectEntity project;
    BigDecimal amount;
    @ManyToOne
    @NonNull CurrencyEntity currency;
    BigDecimal dollarsEquivalent;
    Integer contributionCount;
    Long recipientId;
    String recipientLogin;
    String recipientAvatarUrl;
    Long requestorId;
    String requestorLogin;
    String requestorAvatarUrl;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusDataEntity statusData;

    public RewardDetailsView toDomain() {
        return RewardDetailsView.builder()
                .id(id)
                .to(GithubUserIdentity.builder()
                        .githubAvatarUrl(recipientAvatarUrl)
                        .githubLogin(recipientLogin)
                        .githubUserId(recipientId)
                        .build())
                .amount(amount)
                .createdAt(requestedAt)
                .processedAt(statusData.paidAt())
                .currency(currency.toOldDomain())
                .dollarsEquivalent(dollarsEquivalent)
                .statusForUser(status.forUser())
                .statusForProjectLead(status.forProjectLead())
                .from(GithubUserIdentity.builder()
                        .githubUserId(requestorId)
                        .githubLogin(requestorLogin)
                        .githubAvatarUrl(requestorAvatarUrl)
                        .build())
                .project(project.toDomain())
                .build();
    }

    public UserRewardView toUserReward() {
        return UserRewardView.builder()
                .id(id)
                .projectId(project.getId())
                .requestedAt(requestedAt)
                .processedAt(statusData.paidAt())
                .rewardedOnProjectName(project.getName())
                .rewardedOnProjectLogoUrl(project.getLogoUrl())
                .status(status.forUser())
                .amount(UserRewardView.Amount.builder()
                        .total(amount)
                        .currency(currency.toOldDomain())
                        .dollarsEquivalent(dollarsEquivalent)
                        .build())
                .numberOfRewardedContributions(contributionCount)
                .build();
    }

    public ProjectRewardView toProjectReward() {
        return ProjectRewardView.builder()
                .id(id)
                .numberOfRewardedContributions(contributionCount)
                .requestedAt(requestedAt)
                .processedAt(statusData.paidAt())
                .rewardedUserLogin(requestorLogin)
                .rewardedUserAvatar(requestorAvatarUrl)
                .status(status.forProjectLead())
                .amount(ProjectRewardView.Amount.builder()
                        .total(amount)
                        .currency(currency.toOldDomain())
                        .dollarsEquivalent(dollarsEquivalent)
                        .build())
                .numberOfRewardedContributions(contributionCount)
                .build();
    }
}
