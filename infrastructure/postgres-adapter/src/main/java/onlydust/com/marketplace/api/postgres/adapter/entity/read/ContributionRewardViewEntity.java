package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.view.ContributionRewardView;
import onlydust.com.marketplace.project.domain.view.Money;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContributionRewardViewEntity {

    @Id
    UUID id;
    Date requestedAt;
    BigDecimal amount;
    @ManyToOne
    CurrencyEntity currency;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusDataEntity statusData;

    String requestorLogin;
    String requestorAvatarUrl;
    Long requestorId;
    @NonNull
    UUID projectId;

    String recipientLogin;
    String recipientAvatarUrl;
    Long recipientId;

    UUID billingProfileId;

    public ContributionRewardView toView() {
        final var requestor = GithubUserIdentity.builder()
                .githubLogin(requestorLogin)
                .githubAvatarUrl(requestorAvatarUrl)
                .githubUserId(requestorId)
                .build();
        final var recipient = GithubUserIdentity.builder()
                .githubLogin(recipientLogin)
                .githubAvatarUrl(recipientAvatarUrl)
                .githubUserId(recipientId)
                .build();

        return ContributionRewardView.builder()
                .id(id)
                .amount(new Money(amount, currency.toView())
                        .dollarsEquivalentValue(statusData.amountUsdEquivalent())
                        .usdConversionRateValue(statusData.usdConversionRate()))
                .status(status())
                .from(requestor)
                .to(recipient)
                .createdAt(requestedAt)
                .processedAt(statusData.paidAt())
                .billingProfileId(billingProfileId)
                .build();
    }

    private RewardStatus status() {
        return RewardStatus.builder()
                .projectId(projectId)
                .billingProfileId(billingProfileId)
                .recipientId(recipientId)
                .status(this.status.toDomain())
                .build();
    }
}
