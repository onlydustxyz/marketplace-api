package onlydust.com.marketplace.bff.read.entities.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.contract.model.ContributorPageItemResponse;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.RewardTotalAmountsResponse;
import onlydust.com.marketplace.api.contract.model.ShortCurrencyResponse;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class ProjectContributorsQueryEntity {
    @Id
    @Column(name = "id")
    Long githubUserId;
    @Column(name = "login")
    String login;
    @Column(name = "avatar_url")
    String avatarUrl;
    @Column(name = "contribution_count")
    Integer contributionCount;
    @Column(name = "is_registered")
    boolean isRegistered;
    boolean isHidden;
    @Column(name = "earned")
    BigDecimal earned;
    @Column(name = "reward_count")
    Integer rewards;
    @Column(name = "to_reward_count")
    Integer totalToReward;
    @Column(name = "prs_to_reward")
    Integer prsToReward;
    @Column(name = "code_reviews_to_reward")
    Integer codeReviewsToReward;
    @Column(name = "issues_to_reward")
    Integer issuesToReward;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "totals_earned")
    private List<TotalEarnedPerCurrency> totalEarnedPerCurrencies;

    public ContributorPageItemResponse toDto(boolean includeDataForLead) {
        final var dto = new ContributorPageItemResponse()
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .isRegistered(isRegistered)
                .contributionCount(contributionCount)
                .rewardCount(rewards)
                .earned(new RewardTotalAmountsResponse()
                        .totalAmount(earned)
                        .details(isNull(totalEarnedPerCurrencies) ? List.of() : totalEarnedPerCurrencies.stream().map(TotalEarnedPerCurrency::toDto).toList()))
                .hidden(isHidden);

        if (includeDataForLead) {
            dto.setContributionToRewardCount(totalToReward);
            dto.setPullRequestToReward(prsToReward);
            dto.setIssueToReward(issuesToReward);
            dto.setCodeReviewToReward(codeReviewsToReward);
        }
        return dto;
    }

    @Data
    @NoArgsConstructor
    public static class TotalEarnedPerCurrency {
        @JsonProperty("total_dollars_equivalent")
        BigDecimal totalDollarsEquivalent;
        @JsonProperty("total_amount")
        BigDecimal totalAmount;
        @JsonProperty("currency_id")
        UUID currencyId;
        @JsonProperty("currency_code")
        String currencyCode;
        @JsonProperty("currency_name")
        String currencyName;
        @JsonProperty("currency_decimals")
        Integer currencyDecimals;
        @JsonProperty("currency_latest_usd_quote")
        BigDecimal currencyLatestUsdQuote;
        @JsonProperty("currency_logo_url")
        String logoUrl;

        public Money toDto() {
            return new Money()
                    .amount(totalAmount)
                    .currency(new ShortCurrencyResponse()
                            .id(currencyId)
                            .code(currencyCode)
                            .name(currencyName)
                            .decimals(currencyDecimals)
                            .logoUrl(logoUrl != null ? URI.create(logoUrl) : null))
                    .prettyAmount(pretty(totalAmount, currencyDecimals, currencyLatestUsdQuote))
                    .usdConversionRate(currencyLatestUsdQuote)
                    .usdEquivalent(prettyUsd(totalDollarsEquivalent));
        }
    }
}
