package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileRewardView;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardShortView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Value
@NoArgsConstructor(force = true)
@Table(name = "rewards", schema = "public")
@Accessors(fluent = true)
public class RewardViewEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    BigDecimal amount;
    @NonNull
    Date requestedAt;

    @ManyToOne
    @JoinColumn(name = "requestorId", referencedColumnName = "id")
    @NonNull
    UserViewEntity requestor;

    @ManyToOne
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId")
    @NonNull
    AllUserViewEntity recipient;

    @ManyToOne
    @JoinColumn(name = "billingProfileId")
    BillingProfileEntity billingProfile;

    @ManyToOne
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyEntity currency;

    @ManyToOne
    @JoinColumn(name = "projectId")
    @NonNull
    ProjectEntity project;

    @OneToMany(mappedBy = "rewardId", fetch = FetchType.EAGER)
    @NonNull
    List<RewardItemEntity> rewardItems;

    @ManyToOne
    @JoinColumn(name = "invoiceId")
    InvoiceEntity invoice;

    @ManyToMany
    @JoinTable(name = "rewards_receipts", schema = "accounting",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    Set<ReceiptEntity> receipts = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusEntity status;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusDataEntity statusData;

    public RewardShortView toShortView() {
        return RewardShortView.builder()
                .id(RewardId.of(id))
                .status(status())
                .project(project.toView())
                .money(new MoneyView(amount, currency.toDomain(), statusData.usdConversionRate(), statusData.amountUsdEquivalent()))
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
                .status(status())
                .unlockDate(statusData.unlockDate())
                .amount(new MoneyView(amount, currency.toDomain(), statusData.usdConversionRate(), statusData.amountUsdEquivalent()))
                .numberOfRewardedContributions(rewardItems.size())
                .recipientAvatarUrl(recipient.avatarUrl())
                .recipientId(recipient.githubUserId())
                .recipientLogin(recipient.login())
                .billingProfileId(billingProfile == null ? null : billingProfile.getId())
                .build();
    }

    private ContributorLinkView to() {
        return ContributorLinkView.builder()
                .avatarUrl(recipient.avatarUrl())
                .login(recipient.login())
                .githubUserId(recipient.githubUserId())
                .isRegistered(recipient.isRegistered())
                .build();
    }

    private ContributorLinkView from() {
        return ContributorLinkView.builder()
                .githubUserId(requestor.githubUserId())
                .login(requestor.login())
                .avatarUrl(requestor.avatarUrl())
                .isRegistered(true)
                .build();
    }

    public RewardDetailsView toView() {
        return RewardDetailsView.builder()
                .id(id)
                .to(to())
                .amount(new Money(amount, currency.toView())
                        .dollarsEquivalentValue(statusData.amountUsdEquivalent())
                        .usdConversionRateValue(statusData.usdConversionRate()))
                .createdAt(requestedAt)
                .processedAt(statusData.paidAt())
                .status(status())
                .unlockDate(statusData.unlockDate())
                .from(from())
                .project(project.toDomain())
                .receipt(receipts.stream().findFirst().map(ReceiptEntity::toView).orElse(null))
                .billingProfileId(billingProfile == null ? null : billingProfile.getId())
                .build();
    }

    public RewardStatus status() {
        return RewardStatus.builder()
                .projectId(project.getId())
                .billingProfileId(billingProfile == null ? null : billingProfile.getId())
                .recipientId(recipient.githubUserId())
                .status(this.status.toDomain())
                .build();
    }
}
