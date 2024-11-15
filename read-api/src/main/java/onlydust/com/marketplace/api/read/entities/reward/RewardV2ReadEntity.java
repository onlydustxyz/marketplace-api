package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.kernel.mapper.AmountMapper;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@Immutable
public class RewardV2ReadEntity {
    @Id
    UUID id;
    @NonNull
    UUID projectId;
    @NonNull
    UUID billingProfileId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "accounting.reward_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull
    RewardStatus.Input status;
    @ManyToOne
    @JoinColumn(name = "currency_id")
    @NonNull
    CurrencyReadEntity currency;
    @NonNull
    BigDecimal amount;
    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ContributorResponse requestor;
    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ContributorResponse recipient;
    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ProjectLinkResponse project;
    @NonNull
    ZonedDateTime requestedAt;
    ZonedDateTime invoicedAt;
    ZonedDateTime processedAt;
    ZonedDateTime unlockDate;

    BigDecimal amountUsdEquivalent;
    BigDecimal usdConversionRate;

    UUID invoiceId;
    @JdbcTypeCode(SqlTypes.ARRAY)
    UUID[] contributionUuids;

    @JdbcTypeCode(SqlTypes.JSON)
    Receipt receipt;


    public RewardPageItemResponse toDto(final AuthenticatedUser caller, final MetaBlockExplorer blockExplorer) {
        return new RewardPageItemResponse()
                .id(id)
                .status(statusAsUser(caller))
                .amount(amount())
                .from(requestor)
                .to(recipient)
                .requestedAt(requestedAt)
                .invoicedAt(invoicedAt)
                .processedAt(processedAt)
                .unlockDate(unlockDate)
                .billingProfileId(billingProfileId)
                .project(project)
                .invoiceId(invoiceId)
                .items(contributionUuids == null ? List.of() : List.of(contributionUuids))
                .transactionReference(receipt == null ? null : receipt.transactionReference)
                .transactionReferenceLink(receipt == null || receipt.blockchain() == null ? null
                        : blockExplorer.url(receipt.blockchain(), receipt.transactionReference))
                ;
    }

    private Money amount() {
        return new Money()
                .amount(amount)
                .currency(new ShortCurrencyResponse()
                        .name(currency.name())
                        .code(currency.code())
                        .decimals(currency.decimals())
                        .logoUrl(isNull(currency.logoUrl()) ? null : URI.create(currency.logoUrl()))
                        .id(currency.id())
                )
                .usdConversionRate(usdConversionRate)
                .usdEquivalent(AmountMapper.prettyUsd(amountUsdEquivalent))
                .prettyAmount(AmountMapper.pretty(amount, currency.decimals(), isNull(currency.latestUsdQuote()) ? null : currency.latestUsdQuote().getPrice()))
                ;
    }

    private RewardStatusContract statusAsUser(final AuthenticatedUser user) {
        final RewardStatus.Output output = RewardStatus.builder()
                .projectId(projectId)
                .billingProfileId(billingProfileId)
                .recipientId(recipient.getGithubUserId())
                .status(status)
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

    public record Receipt(
            UUID id,
            ZonedDateTime createdAt,
            NetworkEnumEntity network,
            String thirdPartyName,
            String thirdPartyAccountNumber,
            String transactionReference) {

        public Blockchain blockchain() {
            return switch (network) {
                case ETHEREUM -> Blockchain.ETHEREUM;
                case APTOS -> Blockchain.APTOS;
                case OPTIMISM -> Blockchain.OPTIMISM;
                case STARKNET -> Blockchain.STARKNET;
                case STELLAR -> Blockchain.STELLAR;
                case NEAR -> Blockchain.NEAR;
                case SEPA -> null;
            };
        }
    }
}
