package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;

import static java.util.Objects.nonNull;

public interface UserPayoutInfoMapper {

    static UserPayoutInformationResponse userPayoutInformationToResponse(UserPayoutInformation view) {
        final UserPayoutInformationResponse contract = new UserPayoutInformationResponse();
        final Boolean isACompany = view.getIsACompany();
        contract.setIsCompany(isACompany);
        contract.setHasValidContactInfo(view.hasValidContactInfo());
        if (isACompany) {
            final CompanyIdentity company = new CompanyIdentity();
            company.setName(view.getCompany().getName());
            company.setIdentificationNumber(view.getCompany().getIdentificationNumber());
            final PersonIdentity owner = new PersonIdentity();
            owner.setFirstname(view.getCompany().getOwner().getFirstName());
            owner.setLastname(view.getCompany().getOwner().getLastName());
            company.setOwner(owner);
            contract.setCompany(company);
        } else if (nonNull(view.getPerson())) {
            final PersonIdentity person = new PersonIdentity();
            person.setFirstname(view.getPerson().getFirstName());
            person.setLastname(view.getPerson().getLastName());
            contract.setPerson(person);
        }
        if (nonNull(view.getPayoutSettings())) {
            final UserPayoutInformationResponsePayoutSettings payoutSettings =
                    getUserPayoutInformationContractPayoutSettings(view);
            contract.setPayoutSettings(payoutSettings);
        }
        final UserPayoutInformationResponseLocation location = new UserPayoutInformationResponseLocation();
        if (nonNull(view.getLocation())) {
            location.setAddress(view.getLocation().getAddress());
            location.setCity(view.getLocation().getCity());
            location.setCountry(view.getLocation().getCountry());
            location.setPostalCode(view.getLocation().getPostalCode());
            contract.setLocation(location);
        }
        return contract;
    }

    private static UserPayoutInformationResponsePayoutSettings getUserPayoutInformationContractPayoutSettings(UserPayoutInformation view) {
        final var payoutSettings = new UserPayoutInformationResponsePayoutSettings();
        payoutSettings.setHasValidPayoutSettings(view.hasValidPayoutSettings());
        payoutSettings.setEthAddress(view.getPayoutSettings().getEthAddress());
        payoutSettings.setEthName(view.getPayoutSettings().getEthName());
        payoutSettings.setAptosAddress(view.getPayoutSettings().getAptosAddress());
        payoutSettings.setOptimismAddress(view.getPayoutSettings().getOptimismAddress());
        payoutSettings.setStarknetAddress(view.getPayoutSettings().getStarknetAddress());
        if (nonNull(view.getPayoutSettings().getSepaAccount())) {
            final var sepaAccount = new UserPayoutInformationResponsePayoutSettingsSepaAccount();
            sepaAccount.setBic(view.getPayoutSettings().getSepaAccount().getBic());
            sepaAccount.setIban(view.getPayoutSettings().getSepaAccount().getIban());
            payoutSettings.setSepaAccount(sepaAccount);
        }

        payoutSettings.missingEthWallet(view.isMissingEthereumWallet());
        payoutSettings.missingAptosWallet(view.isMissingAptosWallet());
        payoutSettings.missingOptimismWallet(view.isMissingOptimismWallet());
        payoutSettings.missingStarknetWallet(view.isMissingStarknetWallet());
        payoutSettings.missingSepaAccount(view.isMissingSepaAccount());
        return payoutSettings;
    }

    static UserPayoutInformation userPayoutInformationToDomain(final UserPayoutInformationRequest contract) {
        final Boolean isCompany = contract.getIsCompany();
        UserPayoutInformation userPayoutInformation = UserPayoutInformation.builder()
                .isACompany(isCompany)
                .build();
        if (isCompany) {
            userPayoutInformation = userPayoutInformation.toBuilder().company(UserPayoutInformation.Company.builder()
                            .identificationNumber(contract.getCompany().getIdentificationNumber())
                            .name(contract.getCompany().getName())
                            .owner(UserPayoutInformation.Person.builder()
                                    .lastName(contract.getCompany().getOwner().getLastname())
                                    .firstName(contract.getCompany().getOwner().getFirstname())
                                    .build())
                            .build())
                    .build();
        } else if (nonNull(contract.getPerson())) {
            userPayoutInformation = userPayoutInformation.toBuilder().person(
                            UserPayoutInformation.Person.builder()
                                    .lastName(contract.getPerson().getLastname())
                                    .firstName(contract.getPerson().getFirstname())
                                    .build())
                    .build();

        }
        userPayoutInformation = userPayoutInformation.toBuilder()
                .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                        .starknetAddress(contract.getPayoutSettings().getStarknetAddress())
                        .aptosAddress(contract.getPayoutSettings().getAptosAddress())
                        .optimismAddress(contract.getPayoutSettings().getOptimismAddress())
                        .ethAddress(contract.getPayoutSettings().getEthAddress())
                        .ethName(contract.getPayoutSettings().getEthName())
                        .build())
                .build();
        if (nonNull(contract.getPayoutSettings().getSepaAccount())) {
            userPayoutInformation = userPayoutInformation.toBuilder()
                    .payoutSettings(userPayoutInformation.getPayoutSettings().toBuilder()
                            .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                                    .bic(contract.getPayoutSettings().getSepaAccount().getBic())
                                    .iban(contract.getPayoutSettings().getSepaAccount().getIban())
                                    .build())
                            .build())
                    .build();
        }
        userPayoutInformation = userPayoutInformation.toBuilder()
                .location(
                        UserPayoutInformation.Location.builder()
                                .address(contract.getLocation().getAddress())
                                .country(contract.getLocation().getCountry())
                                .city(contract.getLocation().getCity())
                                .postalCode(contract.getLocation().getPostalCode())
                                .build()
                )
                .build();
        return userPayoutInformation;
    }
}
