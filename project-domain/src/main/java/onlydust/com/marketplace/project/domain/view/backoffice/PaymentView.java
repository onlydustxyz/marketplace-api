package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings.SepaAccount;

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
            case USD -> Optional.ofNullable(recipientSepaAccount).map(SepaAccount::valid).orElse(false);
            case ETH, LORDS, USDC -> nonNull(recipientEthWallet);
            case OP -> nonNull(recipientOptimismWallet);
            case APT -> nonNull(recipientAptosWallet);
            case STRK -> nonNull(recipientStarkWallet);
        };
    }

    public String recipientPayoutSettings() {
        return switch (currency) {
            case USD -> recipientSepaAccount == null ? "" :
                    "%s / %s".formatted(recipientSepaAccount.getAccountNumber().asString(), recipientSepaAccount.getBic());
            case ETH, LORDS, USDC -> recipientEthWallet;
            case OP -> recipientOptimismWallet;
            case APT -> recipientAptosWallet;
            case STRK -> recipientStarkWallet;
        };
    }
}
