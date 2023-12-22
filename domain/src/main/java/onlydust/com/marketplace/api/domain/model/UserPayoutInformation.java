package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Data
@Builder(toBuilder = true)
public class UserPayoutInformation {
    private static final Pattern ETHEREUM_WALLET_PATTERN = Pattern.compile("^0[xX][a-fA-F0-9]{40}$");
    private static final Pattern APTOS_WALLET_PATTERN = Pattern.compile("^0[xX][a-fA-F0-9]{64}$");
    private static final Pattern OPTIMISM_WALLET_PATTERN = Pattern.compile("^0[xX][a-fA-F0-9]{40}$");
    private static final Pattern STARKNET_WALLET_PATTERN = Pattern.compile("^0[xX][a-fA-F0-9]{64}$");

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

    private boolean hasValidPayoutSettings() {
        return nonNull(payoutSettings) && pendingPaymentsCurrencies.stream().allMatch(currency -> switch (currency) {
            case Usd -> nonNull(payoutSettings.sepaAccount) && payoutSettings.sepaAccount.valid();
            case Eth, Lords, Usdc -> isNotEmpty(payoutSettings.ethAddress) || isNotEmpty(payoutSettings.ethName);
            case Apt -> isNotEmpty(payoutSettings.aptosAddress);
            case Op -> isNotEmpty(payoutSettings.optimismAddress);
            case Stark -> isNotEmpty(payoutSettings.starknetAddress);
        });
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

    public void validateWalletAddresses() {
        if (nonNull(this.payoutSettings)) {
            if (nonNull(this.payoutSettings.aptosAddress) && !APTOS_WALLET_PATTERN.matcher(this.payoutSettings.aptosAddress).matches()) {
                throw OnlyDustException.badRequest("Invalid Aptos address format (%s)".formatted(this.payoutSettings.aptosAddress));
            }
            if (nonNull(this.payoutSettings.starknetAddress) && !STARKNET_WALLET_PATTERN.matcher(this.payoutSettings.starknetAddress).matches()) {
                throw OnlyDustException.badRequest("Invalid Starknet address format (%s)".formatted(this.payoutSettings.starknetAddress));
            }
            if (nonNull(this.payoutSettings.ethAddress) && !ETHEREUM_WALLET_PATTERN.matcher(this.payoutSettings.ethAddress).matches()) {
                throw OnlyDustException.badRequest("Invalid Ethereum address format (%s)".formatted(this.payoutSettings.ethAddress));
            }
            if (nonNull(this.payoutSettings.optimismAddress) && !OPTIMISM_WALLET_PATTERN.matcher(this.payoutSettings.optimismAddress).matches()) {
                throw OnlyDustException.badRequest("Invalid Optimism address format (%s)".formatted(this.payoutSettings.optimismAddress));
            }
        }
    }

    public enum UsdPreferredMethodEnum {
        FIAT, CRYPTO
    }

    @Data
    @Builder(toBuilder = true)
    public static class PayoutSettings {
        String ethAddress;
        String ethName;
        String optimismAddress;
        String aptosAddress;
        String starknetAddress;
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
        String iban;

        public boolean valid() {
            return nonNull(bic) && nonNull(iban);
        }
    }
}
