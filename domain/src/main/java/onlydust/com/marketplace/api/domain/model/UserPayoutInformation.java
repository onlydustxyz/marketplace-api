package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;

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
    Boolean hasValidPerson = true;
    @Builder.Default
    Boolean hasValidCompany = true;
    @Builder.Default
    Boolean hasValidLocation = true;

    public Boolean getHasValidContactInfo() {
        return (hasValidCompany || hasValidPerson) && hasValidLocation;
    }

    public boolean isValid() {
        return isNull(this.payoutSettings) ? this.getHasValidContactInfo() :
                this.getHasValidContactInfo() && this.payoutSettings.isValid();
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
        @Builder.Default
        Boolean hasMissingEthWallet = false;
        String optimismAddress;
        @Builder.Default
        Boolean hasMissingOptimismWallet = false;
        String aptosAddress;
        @Builder.Default
        Boolean hasMissingAptosWallet = false;
        String starknetAddress;
        @Builder.Default
        Boolean hasMissingStarknetWallet = false;
        SepaAccount sepaAccount;
        @Builder.Default
        Boolean hasMissingBankingAccount = false;

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
