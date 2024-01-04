package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.Company;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.Location;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.Person;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.SepaAccount;

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
    Identity recipientIdentity;
    Location recipientLocation;
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

    public record Identity(Company company, Person person) {
        public boolean valid() {
            return Optional.ofNullable(company).map(Company::valid).orElse(false) ||
                   Optional.ofNullable(person).map(Person::valid).orElse(false);
        }
    }

    public Boolean recipientPayoutInfoValid() {
        if (!Optional.ofNullable(recipientIdentity).map(Identity::valid).orElse(false)) {
            return false;
        }

        if (!Optional.ofNullable(recipientLocation).map(Location::valid).orElse(false)) {
            return false;
        }

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
            case Usd ->
                    "%s / %s".formatted(recipientSepaAccount.getIban().asString(), recipientSepaAccount.getBic());
            case Eth, Lords, Usdc -> recipientEthWallet;
            case Op -> recipientOptimismWallet;
            case Apt -> recipientAptosWallet;
            case Strk -> recipientStarkWallet;
        };
    }
}
