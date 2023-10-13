package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class UserPayoutInformation {
    Person person;
    Company company;
    Boolean isACompany;
    Location location;
    PayoutSettings payoutSettings;


    @Data
    @Builder
    public static class PayoutSettings {
        UsdPreferredMethodEnum usdPreferredMethodEnum;
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
