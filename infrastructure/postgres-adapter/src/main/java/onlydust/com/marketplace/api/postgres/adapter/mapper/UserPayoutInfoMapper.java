package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.CompanyJsonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.LocationJsonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.PersonJsonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.UsdPreferredMethodEnumEntity;

import static java.util.Objects.nonNull;

public interface UserPayoutInfoMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static UserPayoutInformation mapEntityToDomain(final UserPayoutInfoEntity userPayoutInfoEntity) {
        try {
            UserPayoutInformation userPayoutInformation = UserPayoutInformation.builder().build();
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
            if (nonNull(userPayoutInfoEntity.getIdentity())) {
                final JsonNode identity = userPayoutInfoEntity.getIdentity();
                if (identity.has("Person")) {
                    final PersonJsonEntity personJsonEntity = OBJECT_MAPPER.treeToValue(identity,
                            PersonJsonEntity.class);
                    userPayoutInformation = userPayoutInformation.toBuilder()
                            .person(UserPayoutInformation.Person.builder()
                                    .firstName(personJsonEntity.getFirstName())
                                    .lastName(personJsonEntity.getLastName())
                                    .build())
                            .build();
                } else if (identity.has("Company")) {
                    final CompanyJsonEntity companyJsonEntity = OBJECT_MAPPER.treeToValue(identity,
                            CompanyJsonEntity.class);
                    final UserPayoutInformation.Person person = UserPayoutInformation.Person.builder()
                            .lastName(companyJsonEntity.getOwner().getLastName())
                            .firstName(companyJsonEntity.getOwner().getFirstName())
                            .build();
                    userPayoutInformation = userPayoutInformation.toBuilder()
                            .isACompany(true)
                            .person(person)
                            .company(
                                    UserPayoutInformation.Company.builder()
                                            .owner(person)
                                            .name(companyJsonEntity.getName())
                                            .identificationNumber(companyJsonEntity.getIdentificationNumber())
                                            .build()
                            )
                            .build();
                }
            }
            if (nonNull(userPayoutInfoEntity.getUsdPreferredMethodEnum())) {
                userPayoutInformation = userPayoutInformation.toBuilder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .usdPreferredMethodEnum(usdPreferredMethodToDomain(userPayoutInfoEntity.getUsdPreferredMethodEnum()))
                                .build())
                        .build();
            }

            return userPayoutInformation;
        } catch (JsonProcessingException e) {
            throw OnlydustException.internalServerError(e);
        }
    }


    private static UserPayoutInformation.UsdPreferredMethodEnum usdPreferredMethodToDomain(final UsdPreferredMethodEnumEntity usdPreferredMethodEnumEntity) {
        return switch (usdPreferredMethodEnumEntity) {
            case crypto -> UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO;
            case fiat -> UserPayoutInformation.UsdPreferredMethodEnum.FIAT;
        };
    }
}
