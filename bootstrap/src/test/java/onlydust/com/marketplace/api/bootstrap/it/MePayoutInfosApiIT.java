package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

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
                .jsonPath("$.payoutSettings.usdPreferredMethod").isEqualTo("USDC");
    }


    @Test
    void should_update_user_payout_infos() throws JsonProcessingException {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
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
                        .aptosAddress(faker.rickAndMorty().character())
                        .sepaAccount(new UserPayoutInformationContractPayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban(faker.name().bloodGroup())
                        )
                        .usdPreferredMethod(UserPayoutInformationContractPayoutSettings.UsdPreferredMethodEnum.FIAT)
                );


        // When
        client.put()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(UserPayoutInformationContract.class).equals(requestBody);
    }
}
