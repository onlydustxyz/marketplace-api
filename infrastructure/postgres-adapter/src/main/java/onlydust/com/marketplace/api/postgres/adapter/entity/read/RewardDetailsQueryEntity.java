package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true)
@Entity
@Immutable
public class RewardDetailsQueryEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    Date requestedAt;
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    @NonNull
    ProjectViewEntity project;
    @NonNull
    BigDecimal amount;
    @ManyToOne
    @NonNull
    CurrencyViewEntity currency;
    Integer contributionCount;
    @NonNull
    Long recipientId;
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
    @NonNull
    RewardStatusViewEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusDataViewEntity statusData;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "rewards_receipts",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    Set<ReceiptViewEntity> receipts = Set.of();

    private ContributorLinkView to() {
        return ContributorLinkView.builder()
                .avatarUrl(recipientAvatarUrl)
                .login(recipientLogin)
                .githubUserId(recipientId)
                .isRegistered(recipientIsRegistered)
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
        return new Money(amount, currency.toView())
                .dollarsEquivalentValue(statusData.amountUsdEquivalent())
                .usdConversionRateValue(statusData.usdConversionRate());
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