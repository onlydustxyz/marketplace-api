package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.model.bank.IBAN;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.blockchain.Aptos;
import onlydust.com.marketplace.api.domain.model.blockchain.Ethereum;
import onlydust.com.marketplace.api.domain.model.blockchain.Optimism;
import onlydust.com.marketplace.api.domain.model.blockchain.StarkNet;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.BankAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WalletEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.*;

import java.util.Arrays;
import java.util.UUID;

import static java.util.Objects.nonNull;

public interface UserPayoutInfoMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static UserPayoutInformation mapEntityToDomain(final UserPayoutInfoEntity userPayoutInfoEntity,
                                                   final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity) {
        try {
            UserPayoutInformation userPayoutInformation = UserPayoutInformation.builder()
                    .pendingPaymentsCurrencies(Arrays.stream(userPayoutInfoValidationEntity.getPaymentRequestsCurrencies())
                            .map(CurrencyEnumEntity::toDomain).toList())
                    .build();
            userPayoutInformation = mapLocationToDomain(userPayoutInfoEntity, userPayoutInformation);
            userPayoutInformation = mapIdentityToDomain(userPayoutInfoEntity, userPayoutInformation);
            UserPayoutInformation.PayoutSettings payoutSettings =
                    UserPayoutInformation.PayoutSettings.builder().build();
            payoutSettings = mapWalletsToDomain(userPayoutInfoEntity, payoutSettings);
            payoutSettings = mapBankingAccountToDomain(userPayoutInfoEntity, payoutSettings);
            userPayoutInformation = userPayoutInformation.toBuilder().payoutSettings(payoutSettings).build();
            return userPayoutInformation;
        } catch (JsonProcessingException e) {
            throw OnlyDustException.internalServerError("Failed to map user payout info", e);
        }
    }

    private static UserPayoutInformation.PayoutSettings mapBankingAccountToDomain(UserPayoutInfoEntity userPayoutInfoEntity, UserPayoutInformation.PayoutSettings payoutSettings) {
        if (nonNull(userPayoutInfoEntity.getBankAccount())) {
            payoutSettings = payoutSettings.toBuilder()
                    .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                            .bic(userPayoutInfoEntity.getBankAccount().getBic())
                            .iban(IBAN.of(userPayoutInfoEntity.getBankAccount().getIban()))
                            .build())
                    .build();
        }
        return payoutSettings;
    }

    private static UserPayoutInformation.PayoutSettings mapWalletsToDomain(UserPayoutInfoEntity userPayoutInfoEntity,
                                                                           UserPayoutInformation.PayoutSettings payoutSettings) {
        if (!userPayoutInfoEntity.getWallets().isEmpty()) {
            for (WalletEntity wallet : userPayoutInfoEntity.getWallets()) {
                if (wallet.getId().getNetwork().equals(NetworkEnumEntity.aptos)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .aptosAddress(Aptos.accountAddress(wallet.getAddress()))
                            .build();
                }
                if (wallet.getId().getNetwork().equals(NetworkEnumEntity.starknet)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .starknetAddress(StarkNet.accountAddress(wallet.getAddress()))
                            .build();
                }
                if (wallet.getId().getNetwork().equals(NetworkEnumEntity.optimism)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .optimismAddress(Optimism.accountAddress(wallet.getAddress()))
                            .build();
                }
                if (wallet.getId().getNetwork().equals(NetworkEnumEntity.ethereum)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .ethWallet(Ethereum.wallet(wallet.getAddress()))
                            .build();
                }
            }
        }
        return payoutSettings;
    }

    private static UserPayoutInformation mapLocationToDomain(UserPayoutInfoEntity userPayoutInfoEntity,
                                                             UserPayoutInformation userPayoutInformation) throws JsonProcessingException {
        if (nonNull(userPayoutInfoEntity.getLocation())) {
            final JsonNode location = userPayoutInfoEntity.getLocation();
            final LocationJsonEntity locationJsonEntity;
            locationJsonEntity = OBJECT_MAPPER.treeToValue(location, LocationJsonEntity.class);
            userPayoutInformation =
                    userPayoutInformation.toBuilder().location(UserPayoutInformation.Location.builder()
                            .address(locationJsonEntity.getAddress())
                            .postalCode(locationJsonEntity.getPostCode())
                            .country(locationJsonEntity.getCountry())
                            .city(locationJsonEntity.getCity())
                            .build()).build();
        }
        return userPayoutInformation;
    }

    private static UserPayoutInformation mapIdentityToDomain(UserPayoutInfoEntity userPayoutInfoEntity,
                                                             UserPayoutInformation userPayoutInformation) throws JsonProcessingException {
        if (nonNull(userPayoutInfoEntity.getIdentity())) {
            final JsonNode identity = userPayoutInfoEntity.getIdentity();
            if (identity.has("Person")) {
                final PersonJsonEntity personJsonEntity = OBJECT_MAPPER.treeToValue(identity,
                        PersonJsonEntity.class);
                userPayoutInformation = userPayoutInformation.toBuilder()
                        .person(UserPayoutInformation.Person.builder()
                                .firstName(personJsonEntity.getValue().getFirstName())
                                .lastName(personJsonEntity.getValue().getLastName())
                                .build())
                        .build();
            } else if (identity.has("Company")) {
                final CompanyJsonEntity companyJsonEntity = OBJECT_MAPPER.treeToValue(identity,
                        CompanyJsonEntity.class);
                final UserPayoutInformation.Person person = UserPayoutInformation.Person.builder()
                        .lastName(nonNull(companyJsonEntity.getValue().getOwner()) ?
                                companyJsonEntity.getValue().getOwner().getLastName() : null)
                        .firstName(nonNull(companyJsonEntity.getValue().getOwner()) ?
                                companyJsonEntity.getValue().getOwner().getFirstName() : null)
                        .build();
                userPayoutInformation = userPayoutInformation.toBuilder()
                        .isACompany(true)
                        .company(
                                UserPayoutInformation.Company.builder()
                                        .owner(person)
                                        .name(companyJsonEntity.getValue().getName())
                                        .identificationNumber(companyJsonEntity.getValue().getIdentificationNumber())
                                        .build()
                        )
                        .build();
            }
        }
        return userPayoutInformation;
    }

    static UserPayoutInfoEntity mapDomainToEntity(UUID userId, UserPayoutInformation userPayoutInformation) {
        UserPayoutInfoEntity entity = UserPayoutInfoEntity.builder()
                .userId(userId)
                .build();
        entity = mapIdentityToEntity(userPayoutInformation, entity);
        final UserPayoutInformation.PayoutSettings payoutSettings = userPayoutInformation.getPayoutSettings();
        if (nonNull(payoutSettings)) {
            if (nonNull(payoutSettings.getSepaAccount())) {
                entity = entity.toBuilder()
                        .bankAccount(BankAccountEntity.builder()
                                .bic(userPayoutInformation.getPayoutSettings().getSepaAccount().getBic())
                                .iban(userPayoutInformation.getPayoutSettings().getSepaAccount().getIban().asString())
                                .userId(userId)
                                .build())
                        .build();
            }
            mapWalletsToEntity(userId, payoutSettings, entity);
        }
        if (nonNull(userPayoutInformation.getLocation())) {
            final LocationJsonEntity locationJsonEntity = LocationJsonEntity.builder()
                    .address(userPayoutInformation.getLocation().getAddress())
                    .city(userPayoutInformation.getLocation().getCity())
                    .country(userPayoutInformation.getLocation().getCountry())
                    .postCode(userPayoutInformation.getLocation().getPostalCode())
                    .build();
            entity = entity.toBuilder().location(OBJECT_MAPPER.valueToTree(locationJsonEntity)).build();
        }
        return entity;
    }

    private static UserPayoutInfoEntity mapIdentityToEntity(UserPayoutInformation userPayoutInformation,
                                                            UserPayoutInfoEntity entity) {
        if (userPayoutInformation.getIsACompany() && nonNull(userPayoutInformation.getCompany())) {
            PersonJsonEntity.Value owner = null;
            if (nonNull(userPayoutInformation.getCompany().getOwner())) {
                owner = PersonJsonEntity.Value.builder()
                        .firstName(userPayoutInformation.getCompany().getOwner().getFirstName())
                        .lastName(userPayoutInformation.getCompany().getOwner().getLastName())
                        .build();
            }
            final CompanyJsonEntity company = CompanyJsonEntity.builder()
                    .value(CompanyJsonEntity.Value.builder()
                            .identificationNumber(userPayoutInformation.getCompany().getIdentificationNumber())
                            .name(userPayoutInformation.getCompany().getName())
                            .owner(owner)
                            .build())
                    .build();
            entity = entity.toBuilder().identity(OBJECT_MAPPER.valueToTree(company)).build();

        } else if (nonNull(userPayoutInformation.getPerson())) {
            final PersonJsonEntity personJsonEntity = PersonJsonEntity.builder().value(PersonJsonEntity.Value.builder()
                            .firstName(userPayoutInformation.getPerson().getFirstName())
                            .lastName(userPayoutInformation.getPerson().getLastName())
                            .build())
                    .build();
            entity = entity.toBuilder().identity(OBJECT_MAPPER.valueToTree(personJsonEntity)).build();
        }
        return entity;
    }

    private static void mapWalletsToEntity(UUID userId, UserPayoutInformation.PayoutSettings payoutSettings,
                                           UserPayoutInfoEntity entity) {
        if (nonNull(payoutSettings.getAptosAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getAptosAddress().asString())
                    .type(WalletTypeEnumEntity.address)
                    .id(WalletIdEntity.builder()
                            .userId(userId)
                            .network(NetworkEnumEntity.aptos)
                            .build())
                    .build());
        }
        if (nonNull(payoutSettings.getOptimismAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getOptimismAddress().asString())
                    .type(WalletTypeEnumEntity.address)
                    .id(WalletIdEntity.builder()
                            .userId(userId)
                            .network(NetworkEnumEntity.optimism)
                            .build())
                    .build());
        }
        if (nonNull(payoutSettings.getStarknetAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getStarknetAddress().asString())
                    .type(WalletTypeEnumEntity.address)
                    .id(WalletIdEntity.builder()
                            .userId(userId)
                            .network(NetworkEnumEntity.starknet)
                            .build())
                    .build());
        }
        if (nonNull(payoutSettings.getEthWallet())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getEthWallet().asString())
                    .type(payoutSettings.getEthWallet().accountAddress().isPresent() ?
                            WalletTypeEnumEntity.address : WalletTypeEnumEntity.name)
                    .id(WalletIdEntity.builder()
                            .userId(userId)
                            .network(NetworkEnumEntity.ethereum)
                            .build())
                    .build());
        }
    }
}
