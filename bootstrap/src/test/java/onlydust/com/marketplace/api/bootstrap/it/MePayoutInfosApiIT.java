package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;

@ActiveProfiles({"hasura_auth"})
public class MePayoutInfosApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_get_user_payout_info() throws JsonProcessingException {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.isCompany").isEqualTo(false)
                .jsonPath("$.person.lastname").isEqualTo("BUISSET")
                .jsonPath("$.person.firstname").isEqualTo("Anthony")
                .jsonPath("$.location.address").isEqualTo("771 chemin de la sine")
                .jsonPath("$.location.city").isEqualTo("Vence")
                .jsonPath("$.location.country").isEqualTo("France")
                .jsonPath("$.location.postalCode").isEqualTo("06140")
                .jsonPath("$.payoutSettings.ethName").isEqualTo("abuisset.eth")
                .jsonPath("$.payoutSettings.usdPreferredMethod").isEqualTo("CRYPTO");
    }


    @Test
    void should_update_user_payout_infos() throws JsonProcessingException {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UserPayoutInformationContract requestBody1 = new UserPayoutInformationContract();
        requestBody1.company(
                        new CompanyIdentity()
                                .identificationNumber(faker.number().digit())
                                .name(faker.name().name())
                                .owner(
                                        new PersonIdentity()
                                                .firstname(faker.name().firstName())
                                                .lastname(faker.name().lastName())
                                )
                )
                .isCompany(true)
                .location(new UserPayoutInformationContractLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationContractPayoutSettings()
                        .aptosAddress("0x" + faker.crypto().md5())
                        .sepaAccount(new UserPayoutInformationContractPayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban(faker.name().bloodGroup())
                        )
                        .usdPreferredMethod(UserPayoutInformationContractPayoutSettings.UsdPreferredMethodEnum.FIAT)
                );
        final UserPayoutInformationContract requestBody2 = new UserPayoutInformationContract();
        requestBody2.company(null)
                .person(new PersonIdentity()
                        .firstname(faker.name().firstName())
                        .lastname(faker.name().lastName()))
                .isCompany(false)
                .location(new UserPayoutInformationContractLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationContractPayoutSettings()
                        .ethAddress("0x" + faker.crypto().md5())
                        .optimismAddress("0x" + faker.crypto().md5())
                        .usdPreferredMethod(UserPayoutInformationContractPayoutSettings.UsdPreferredMethodEnum.CRYPTO)
                );


        // When
        client.put()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody1))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(UserPayoutInformationContract.class).equals(requestBody1);

        client.put()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody2))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(UserPayoutInformationContract.class).equals(requestBody2);
    }

    @Test
    void should_update_user_payout_info_given_impersonate_user() {
        // Given
        final String jwt = userHelper.newFakeUser(UUID.randomUUID(), 2L, faker.rickAndMorty().character(),
                faker.internet().url(), true).jwt();
        userHelper.authenticateUser(2L);
        final String impersonatePierreHeader =
                userHelper.getImpersonationHeaderToImpersonatePierre();

        // When
        final UserPayoutInformationContract requestBody = new UserPayoutInformationContract();
        requestBody.company(
                        new CompanyIdentity()
                                .identificationNumber(faker.number().digit())
                                .name(faker.name().name())
                                .owner(
                                        new PersonIdentity()
                                                .firstname(faker.name().firstName())
                                                .lastname(faker.name().lastName())
                                )
                )
                .isCompany(true)
                .location(new UserPayoutInformationContractLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationContractPayoutSettings()
                        .aptosAddress("0x" + faker.crypto().md5())
                        .sepaAccount(new UserPayoutInformationContractPayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban(faker.name().bloodGroup())
                        )
                        .usdPreferredMethod(UserPayoutInformationContractPayoutSettings.UsdPreferredMethodEnum.FIAT)
                );

        // Then
        client.put()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                .header(IMPERSONATION_HEADER, impersonatePierreHeader)
                .body(BodyInserters.fromValue(requestBody))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(UserPayoutInformationContract.class).equals(requestBody);

        final String pierreJwt = userHelper.authenticatePierre().jwt();

        client.get()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + pierreJwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .equals(requestBody);
    }
}
