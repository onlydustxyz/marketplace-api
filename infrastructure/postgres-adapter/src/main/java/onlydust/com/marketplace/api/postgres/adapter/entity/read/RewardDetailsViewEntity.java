package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ReceiptEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true)
@Entity
public class RewardDetailsViewEntity {
    @Id
    @NonNull UUID id;
    @NonNull Date requestedAt;
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    @NonNull ProjectEntity project;
    @NonNull BigDecimal amount;
    @ManyToOne
    @NonNull CurrencyEntity currency;
    Integer contributionCount;
    @NonNull Long recipientId;
    UUID invoiceId;
    UUID billingProfileId;
    String recipientLogin;
    String recipientAvatarUrl;
    Boolean recipientIsRegistered;
    Long requestorId;
    String requestorLogin;
    String requestorAvatarUrl;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusDataEntity statusData;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "rewards_receipts",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    Set<ReceiptEntity> receipts = Set.of();

    private ContributorLinkView to() {
        return ContributorLinkView.builder()
                .avatarUrl(recipientAvatarUrl)
                .login(recipientLogin)
                .githubUserId(recipientId)
                .isRegistered(recipientIsRegistered)
                .build();
    }

    private ContributorLinkView from() {
        return ContributorLinkView.builder()
                .githubUserId(requestorId)
                .login(requestorLogin)
                .avatarUrl(requestorAvatarUrl)
                .isRegistered(true)
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
                .status(status())
                .unlockDate(statusData.unlockDate())
                .amount(amount())
                .numberOfRewardedContributions(contributionCount)
                .rewardedUser(to())
                .billingProfileId(billingProfileId)
                .build();
    }

    public ProjectRewardView toProjectReward() {
        return ProjectRewardView.builder()
                .id(id)
                .numberOfRewardedContributions(contributionCount)
                .requestedAt(requestedAt)
                .processedAt(statusData.paidAt())
                .rewardedUser(to())
                .status(status())
                .unlockDate(statusData.unlockDate())
                .amount(amount())
                .numberOfRewardedContributions(contributionCount)
                .build();
    }

    private Money amount() {
        return Money.builder()
                .amount(amount)
                .currency(currency.toView())
                .usdEquivalent(statusData.amountUsdEquivalent())
                .build();
    }

    private RewardStatus status() {
        return RewardStatus.builder()
                .projectId(project.getId())
                .billingProfileId(billingProfileId)
                .recipientId(recipientId)
                .status(status.toDomain())
                .build();
    }
}
