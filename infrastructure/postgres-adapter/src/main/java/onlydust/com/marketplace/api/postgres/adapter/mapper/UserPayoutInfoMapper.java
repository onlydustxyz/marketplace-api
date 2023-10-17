package onlydust.com.marketplace.api.postgres.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.LocationJsonEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.UsdPreferredMethodEnumEntity;

import java.util.UUID;

import static java.util.Objects.nonNull;

public interface UserPayoutInfoMapper {
    String PERSON = "person";
    String COMPANY = "company";
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

            }


            return UserPayoutInformation.builder()
                    .payoutSettings(UserPayoutInformation.PayoutSettings.builder().build())
                    .person(UserPayoutInformation.Person.builder().build())
                    .company(UserPayoutInformation.Company.builder().build())
                    .isACompany(false)
                    .build();
        } catch (JsonProcessingException e) {
            throw OnlydustException.internalServerError(e);
        }
    }

    static UserPayoutInfoEntity mapDomainToEntity(final UserPayoutInformation userPayoutInformation,
                                                  final UUID userId) {
        UserPayoutInfoEntity userPayoutInfoEntity = UserPayoutInfoEntity.builder()
                .id(userId)
                .usdPreferredMethodEnum(usdPreferredMethodToEntity(userPayoutInformation.getPayoutSettings().getUsdPreferredMethodEnum()))
                .build();
        if (nonNull(userPayoutInformation.getLocation())) {
            final LocationJsonEntity location = LocationJsonEntity.builder()
                    .address(userPayoutInformation.getLocation().getAddress())
                    .postCode(userPayoutInformation.getLocation().getPostalCode())
                    .country(userPayoutInformation.getLocation().getCountry())
                    .city(userPayoutInformation.getLocation().getCity())
                    .build();
            userPayoutInfoEntity = userPayoutInfoEntity.toBuilder()
                    .location(OBJECT_MAPPER.valueToTree(location))
                    .build();

        }
        if (nonNull(userPayoutInformation.getCompany())) {

        }
        if (nonNull(userPayoutInformation.getPerson())) {

        }
        if (nonNull(userPayoutInformation.getPayoutSettings())) {

        }


        return userPayoutInfoEntity;

    }

    private static UsdPreferredMethodEnumEntity usdPreferredMethodToEntity(final UserPayoutInformation.UsdPreferredMethodEnum usdPreferredMethodEnum) {
        return switch (usdPreferredMethodEnum) {
            case CRYPTO -> UsdPreferredMethodEnumEntity.crypto;
            case FIAT -> UsdPreferredMethodEnumEntity.fiat;
        };
    }

    private static UserPayoutInformation.UsdPreferredMethodEnum usdPreferredMethodToDomain(final UsdPreferredMethodEnumEntity usdPreferredMethodEnumEntity) {
        return switch (usdPreferredMethodEnumEntity) {
            case crypto -> UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO;
            case fiat -> UserPayoutInformation.UsdPreferredMethodEnum.FIAT;
        };
    }
}
