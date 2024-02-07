package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings.SepaAccount;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Value
@Builder
@EqualsAndHashCode
public class PaymentView {
    UUID id;
    UUID budgetId;
    UUID projectId;
    BigDecimal amount;
    Currency currency;
    Long recipientId;
    UUID requestorId;
    List<String> items;
    ZonedDateTime requestedAt;
    ZonedDateTime processedAt;
    Integer pullRequestsCount;
    Integer issuesCount;
    Integer dustyIssuesCount;
    Integer codeReviewsCount;
    //    Identity recipientIdentity;
//    Location recipientLocation;
    SepaAccount recipientSepaAccount;
    String recipientEthWallet;
    String recipientStarkWallet;
    String recipientOptimismWallet;
    String recipientAptosWallet;

    @Value
    @Builder
    public static class Filters {
        @Builder.Default
        List<UUID> projects = List.of();
        @Builder.Default
        List<UUID> payments = List.of();
    }

    public Boolean recipientPayoutInfoValid() {
        // TODO : replace with KYC/KYB verification status

        return switch (currency) {
            case Usd -> Optional.ofNullable(recipientSepaAccount).map(SepaAccount::valid).orElse(false);
            case Eth, Lords, Usdc -> nonNull(recipientEthWallet);
            case Op -> nonNull(recipientOptimismWallet);
            case Apt -> nonNull(recipientAptosWallet);
            case Strk -> nonNull(recipientStarkWallet);
        };
    }

    public String recipientPayoutSettings() {
        return switch (currency) {
            case Usd -> recipientSepaAccount == null ? "" :
                    "%s / %s".formatted(recipientSepaAccount.getAccountNumber().asString(), recipientSepaAccount.getBic());
            case Eth, Lords, Usdc -> recipientEthWallet;
            case Op -> recipientOptimismWallet;
            case Apt -> recipientAptosWallet;
            case Strk -> recipientStarkWallet;
        };
    }
}
