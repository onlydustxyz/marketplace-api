package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.RewardStatusContract;
import onlydust.com.marketplace.api.contract.model.ShortCurrencyResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.kernel.mapper.AmountMapper;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

@Value
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
public class RewardDetailsReadEntity {
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
    CurrencyReadEntity currency;
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
    RewardStatusReadEntity status;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusDataReadEntity statusData;

    @ManyToMany
    @JoinTable(
            name = "rewards_receipts",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    Set<ReceiptReadEntity> receipts = Set.of();

    public ContributorResponse toContributorResponse() {
        return new ContributorResponse()
                .avatarUrl(recipientAvatarUrl)
                .login(recipientLogin)
                .githubUserId(recipientId)
                .isRegistered(recipientIsRegistered);
    }

    public Money amount() {
        return new Money()
                .amount(amount)
                .currency(new ShortCurrencyResponse()
                        .name(currency.name())
                        .code(currency.code())
                        .decimals(currency.decimals())
                        .logoUrl(isNull(currency.logoUrl()) ? null : URI.create(currency.logoUrl()))
                        .id(currency.id())
                )
                .usdConversionRate(statusData.usdConversionRate())
                .usdEquivalent(AmountMapper.prettyUsd(statusData.amountUsdEquivalent()))
                .prettyAmount(AmountMapper.pretty(amount, currency.decimals(), isNull(currency.latestUsdQuote()) ? null : currency.latestUsdQuote().getPrice()))
                ;
    }

    public RewardStatusContract statusAsUser(final AuthenticatedUser user) {
        final RewardStatus.Output output = RewardStatus.builder()
                .projectId(project.getId())
                .billingProfileId(billingProfileId)
                .recipientId(recipientId)
                .status(status.status())
                .build().as(user);
        return switch (output) {
            case PENDING_COMPANY -> RewardStatusContract.PENDING_COMPANY;
            case PENDING_CONTRIBUTOR -> RewardStatusContract.PENDING_CONTRIBUTOR;
            case LOCKED -> RewardStatusContract.LOCKED;
            case PENDING_BILLING_PROFILE -> RewardStatusContract.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatusContract.PENDING_VERIFICATION;
            case COMPLETE -> RewardStatusContract.COMPLETE;
            case GEO_BLOCKED -> RewardStatusContract.GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> RewardStatusContract.INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> RewardStatusContract.PAYOUT_INFO_MISSING;
            case PENDING_REQUEST -> RewardStatusContract.PENDING_REQUEST;
            case PENDING_SIGNUP -> RewardStatusContract.PENDING_SIGNUP;
            case PROCESSING -> RewardStatusContract.PROCESSING;
        };
    }

}
