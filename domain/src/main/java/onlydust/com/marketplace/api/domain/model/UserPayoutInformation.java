package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;

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
    Boolean hasValidPerson;
    Boolean hasValidCompany;
    Boolean hasValidLocation;

    public Boolean getHasValidContactInfo() {
        return (hasValidCompany || hasValidPerson) && hasValidLocation;
    }

    public void validate() {
        if (nonNull(this.payoutSettings)) {
            if (nonNull(this.payoutSettings.aptosAddress)) {
                validateWalletAddress(this.payoutSettings.aptosAddress);
            }
            if (nonNull(this.payoutSettings.starknetAddress)) {
                validateWalletAddress(this.payoutSettings.starknetAddress);
            }
            if (nonNull(this.payoutSettings.ethAddress)) {
                validateWalletAddress(this.payoutSettings.ethAddress);
            }
            if (nonNull(this.payoutSettings.optimismAddress)) {
                validateWalletAddress(this.payoutSettings.optimismAddress);
            }
        }
    }

    private void validateWalletAddress(final String walletAddress) {
        if (!walletAddress.startsWith("0x") && !walletAddress.startsWith("0X")) {
            throw OnlyDustException.badRequest("Invalid wallet address format");
        }
    }

    @Data
    @Builder(toBuilder = true)
    public static class PayoutSettings {
        UsdPreferredMethodEnum usdPreferredMethodEnum;
        String ethAddress;
        String ethName;
        Boolean hasMissingEthWallet;
        String optimismAddress;
        Boolean hasMissingOptimismWallet;
        String aptosAddress;
        Boolean hasMissingAptosWallet;
        String starknetAddress;
        Boolean hasMissingStarknetWallet;
        SepaAccount sepaAccount;
        Boolean hasMissingBankingAccount;

        public Boolean isValid() {
            return !(hasMissingAptosWallet || hasMissingEthWallet || hasMissingStarknetWallet || hasMissingOptimismWallet || hasMissingBankingAccount);
        }
    }

    @Data
    @Builder
    public static class Location {
        String country;
        String city;
        String postalCode;
        String address;
    }

    @Data
    @Builder
    public static class Company {
        String name;
        String identificationNumber;
        Person owner;
    }

    @Data
    @Builder
    public static class Person {
        String firstName;
        String lastName;
    }

    public enum UsdPreferredMethodEnum {
        FIAT, CRYPTO
    }

    @Data
    @Builder
    public static class SepaAccount {
        String bic;
        String iban;
    }
}
