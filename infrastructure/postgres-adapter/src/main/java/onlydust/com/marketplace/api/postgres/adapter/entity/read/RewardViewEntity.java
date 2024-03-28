package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ReceiptEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.project.domain.view.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
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
    Integer contributionCount;
    Long recipientId;
    UUID invoiceId;
    UUID billingProfileId;
    String recipientLogin;
    String recipientAvatarUrl;
    String recipientHtmlUrl;
    Boolean recipientIsRegistered;
    Long requestorId;
    String requestorLogin;
    String requestorAvatarUrl;
    String requestorHtmlUrl;
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

    public RewardDetailsView toDomain() {
        return RewardDetailsView.builder()
                .id(id)
                .to(to())
                .amount(amount)
                .createdAt(requestedAt)
                .processedAt(statusData.paidAt())
                .currency(currency.toView())
                .dollarsEquivalent(statusData.amountUsdEquivalent())
                .status(status.toDomain())
                .unlockDate(statusData.unlockDate())
                .from(from())
                .project(project.toDomain())
                .receipt(receipts.stream().findFirst().map(ReceiptEntity::toView).orElse(null))
                .billingProfileId(billingProfileId)
                .build();
    }

    private ContributorLinkView to() {
        return ContributorLinkView.builder()
                .avatarUrl(recipientAvatarUrl)
                .login(recipientLogin)
                .githubUserId(recipientId)
                .url(recipientHtmlUrl)
                .isRegistered(recipientIsRegistered)
                .build();
    }

    private ContributorLinkView from() {
        return ContributorLinkView.builder()
                .githubUserId(requestorId)
                .login(requestorLogin)
                .avatarUrl(requestorAvatarUrl)
                .url(requestorHtmlUrl)
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
                .status(status.toDomain())
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
                .status(status.toDomain())
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

    public BillingProfileRewardView toBillingProfileReward() {
        return BillingProfileRewardView.builder()
                .id(id)
                .projectId(project.getId())
                .requestedAt(requestedAt)
                .processedAt(statusData.paidAt())
                .rewardedOnProjectName(project.getName())
                .rewardedOnProjectLogoUrl(project.getLogoUrl())
                .status(status.toDomain())
                .unlockDate(statusData.unlockDate())
                .amount(BillingProfileRewardView.Amount.builder()
                        .total(amount)
                        .currency(currency.toView())
                        .dollarsEquivalent(statusData.amountUsdEquivalent())
                        .build())
                .numberOfRewardedContributions(contributionCount)
                .recipientAvatarUrl(recipientAvatarUrl)
                .recipientId(recipientId)
                .recipientLogin(recipientLogin)
                .billingProfileId(billingProfileId)
                .build();
    }
}
