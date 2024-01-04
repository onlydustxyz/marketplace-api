package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum.Wallet;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
public class UserPayoutInformation {
    Person person;
    Company company;
    @Builder.Default
    Boolean isACompany = false;
    Location location;
    PayoutSettings payoutSettings;

    @Builder.Default
    List<Currency> pendingPaymentsCurrencies = new ArrayList<>();

    private boolean hasPendingPayments() {
        return !pendingPaymentsCurrencies.isEmpty();
    }

    private boolean hasValidCompany() {
        return isACompany && nonNull(company) && company.valid();
    }

    private boolean hasValidPerson() {
        return !isACompany && nonNull(person) && person.valid();
    }

    private boolean hasValidLocation() {
        return nonNull(location) && location.valid();
    }

    public boolean hasValidPayoutSettings() {
        return nonNull(payoutSettings) && pendingPaymentsCurrencies.stream().allMatch(currency -> switch (currency) {
            case Usd -> nonNull(payoutSettings.sepaAccount) && payoutSettings.sepaAccount.valid();
            case Eth, Lords, Usdc -> nonNull(payoutSettings.ethWallet);
            case Apt -> nonNull(payoutSettings.aptosAddress);
            case Op -> nonNull(payoutSettings.optimismAddress);
            case Strk -> nonNull(payoutSettings.starknetAddress);
        });
    }

    public boolean isMissingOptimismWallet() {
        return pendingPaymentsCurrencies.contains(Currency.Op) && isNull(payoutSettings.optimismAddress);
    }

    public boolean isMissingAptosWallet() {
        return pendingPaymentsCurrencies.contains(Currency.Apt) && isNull(payoutSettings.aptosAddress);
    }

    public boolean isMissingStarknetWallet() {
        return pendingPaymentsCurrencies.contains(Currency.Strk) && isNull(payoutSettings.starknetAddress);
    }

    public boolean isMissingEthereumWallet() {
        return (pendingPaymentsCurrencies.contains(Currency.Eth)
                || pendingPaymentsCurrencies.contains(Currency.Lords)
                || pendingPaymentsCurrencies.contains(Currency.Usdc))
               && isNull(payoutSettings.ethWallet);
    }

    public boolean isMissingSepaAccount() {
        return pendingPaymentsCurrencies.contains(Currency.Usd) && (isNull(payoutSettings.sepaAccount) || !payoutSettings.sepaAccount.valid());
    }

    public boolean hasValidContactInfo() {
        return (hasValidCompany() || hasValidPerson()) && hasValidLocation();
    }

    public boolean isValid() {
        if (!this.hasPendingPayments()) {
            return true;
        }
        return hasValidContactInfo() && hasValidPayoutSettings();
    }

    @Data
    @Builder(toBuilder = true)
    public static class PayoutSettings {
        Wallet ethWallet;
        onlydust.com.marketplace.api.domain.model.blockchain.evm.AccountAddress optimismAddress;
        onlydust.com.marketplace.api.domain.model.blockchain.aptos.AccountAddress aptosAddress;
        onlydust.com.marketplace.api.domain.model.blockchain.starknet.AccountAddress starknetAddress;
        SepaAccount sepaAccount;
    }

    @Data
    @Builder
    public static class Location {
        String country;
        String city;
        String postalCode;
        String address;

        public boolean valid() {
            return nonNull(country) && nonNull(city) && nonNull(postalCode) && nonNull(address);
        }
    }

    @Data
    @Builder
    public static class Company {
        String name;
        String identificationNumber;
        Person owner;

        public boolean valid() {
            return nonNull(name) && nonNull(identificationNumber) && nonNull(owner) && owner.valid();
        }
    }

    @Data
    @Builder
    public static class Person {
        String firstName;
        String lastName;

        public boolean valid() {
            return nonNull(firstName) && nonNull(lastName);
        }
    }

    @Data
    @Builder
    public static class SepaAccount {
        String bic;
        AccountNumber accountNumber;

        public boolean valid() {
            return nonNull(bic) && nonNull(accountNumber);
        }
    }
}
