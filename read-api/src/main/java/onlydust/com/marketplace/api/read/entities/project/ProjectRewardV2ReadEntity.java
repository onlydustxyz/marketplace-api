package onlydust.com.marketplace.api.read.entities.project;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.GithubUserResponse;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.RewardsPageItemResponseV2;
import onlydust.com.marketplace.api.contract.model.ShortCurrencyResponse;
import onlydust.com.marketplace.kernel.mapper.AmountMapper;


@NoArgsConstructor(force = true)
@AllArgsConstructor
@Entity
@Immutable
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectRewardV2ReadEntity {
    @Id
    UUID id;
    ZonedDateTime requestedAt;
    GithubUserResponse requestor;
    GithubUserResponse recipient;
    BigDecimal amount;
    UUID  currencyId;
    String currencyCode;
    String currencyName;
    String currencyLogoUrl;
    Integer currencyDecimals;
    BigDecimal usdAmount;
    @JdbcTypeCode(SqlTypes.ARRAY)
    UUID[] contributions;

    public RewardsPageItemResponseV2 toResponse() {
        final var conversionRate = usdAmount.compareTo(ZERO) == 0 ? ONE : amount.divide(usdAmount, 2, RoundingMode.HALF_EVEN);
        
        return new RewardsPageItemResponseV2()
                .id(id)
                .requestedAt(requestedAt)
                .from(requestor)
                .to(recipient)
                .amount(new Money()
                        .amount(amount)
                        .currency(new ShortCurrencyResponse()
                                .id(currencyId)
                                .code(currencyCode)
                                .name(currencyName)
                                .logoUrl(currencyLogoUrl == null ? null : URI.create(currencyLogoUrl))
                                .decimals(currencyDecimals))
                        .prettyAmount(AmountMapper.pretty(amount, currencyDecimals, conversionRate))
                        .usdEquivalent(AmountMapper.prettyUsd(usdAmount))
                        .usdConversionRate(conversionRate)
                )
                .contributions(List.of(contributions));
    }
} 
