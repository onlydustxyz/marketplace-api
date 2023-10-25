package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;

@ActiveProfiles({"hasura_auth"})
public class MePayoutInfosApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper userHelper;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    @Test
    void should_get_user_payout_info() {
        // Given
        final HasuraUserHelper.AuthenticatedUser anthony = userHelper.authenticateAnthony();
        final String jwt = anthony.jwt();
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .projectId(UUID.randomUUID())
                                .recipientId(anthony.user().getGithubUserId())
                                .id(UUID.randomUUID())
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .currency(CurrencyEnumEntity.usd)
                                .hoursWorked(1)
                                .requestorId(UUID.randomUUID())
                                .build(),
                        PaymentRequestEntity.builder()
                                .projectId(UUID.randomUUID())
                                .recipientId(anthony.user().getGithubUserId())
                                .id(UUID.randomUUID())
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .currency(CurrencyEnumEntity.eth)
                                .hoursWorked(1)
                                .requestorId(UUID.randomUUID())
                                .build(),
                        PaymentRequestEntity.builder()
                                .projectId(UUID.randomUUID())
                                .recipientId(anthony.user().getGithubUserId())
                                .id(UUID.randomUUID())
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .currency(CurrencyEnumEntity.apt)
                                .hoursWorked(1)
                                .requestorId(UUID.randomUUID())
                                .build(),
                        PaymentRequestEntity.builder()
                                .projectId(UUID.randomUUID())
                                .recipientId(anthony.user().getGithubUserId())
                                .id(UUID.randomUUID())
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .currency(CurrencyEnumEntity.stark)
                                .hoursWorked(1)
                                .requestorId(UUID.randomUUID())
                                .build(),
                        PaymentRequestEntity.builder()
                                .projectId(UUID.randomUUID())
                                .recipientId(anthony.user().getGithubUserId())
                                .id(UUID.randomUUID())
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .currency(CurrencyEnumEntity.op)
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .build()
                )
        );

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
                .jsonPath("$.payoutSettings.usdPreferredMethod").isEqualTo("CRYPTO")
                .jsonPath("$.payoutSettings.missingEthWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingAptosWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingStarknetWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingSepaAccount").isEqualTo(true)
                .jsonPath("$.payoutSettings.hasValidPayoutSettings").isEqualTo(false);
    }


    @Test
    void should_update_user_payout_infos() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UserPayoutInformationRequest requestBody1 = new UserPayoutInformationRequest();
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
                .location(new UserPayoutInformationResponseLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationRequestPayoutSettings()
                        .aptosAddress("0x" + faker.crypto().md5())
                        .sepaAccount(new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban(faker.name().bloodGroup())
                        )
                        .usdPreferredMethod(UserPayoutInformationRequestPayoutSettings.UsdPreferredMethodEnum.FIAT)
                );
        final UserPayoutInformationRequest requestBody2 = new UserPayoutInformationRequest();
        requestBody2.company(null)
                .person(new PersonIdentity()
                        .firstname(faker.name().firstName())
                        .lastname(faker.name().lastName()))
                .isCompany(false)
                .location(new UserPayoutInformationResponseLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationRequestPayoutSettings()
                        .ethAddress("0x" + faker.crypto().md5())
                        .optimismAddress("0x" + faker.crypto().md5())
                        .usdPreferredMethod(UserPayoutInformationRequestPayoutSettings.UsdPreferredMethodEnum.CRYPTO)
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
                .expectBody().equals(requestToExpectedResponse(requestBody1, true, true, true, true, true));

        client.put()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody2))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().equals(requestToExpectedResponse(requestBody2, true, true, true, true, true));
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
        final UserPayoutInformationRequest requestBody = new UserPayoutInformationRequest();
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
                .location(new UserPayoutInformationResponseLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationRequestPayoutSettings()
                        .aptosAddress("0x" + faker.crypto().md5())
                        .sepaAccount(new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban(faker.name().bloodGroup())
                        )
                        .usdPreferredMethod(UserPayoutInformationRequestPayoutSettings.UsdPreferredMethodEnum.FIAT)
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
                .expectBody().equals(requestToExpectedResponse(requestBody, true, true, true, true, true));

        final String pierreJwt = userHelper.authenticatePierre().jwt();

        client.get()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + pierreJwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .equals(requestToExpectedResponse(requestBody, true, true, true, true, true));
    }

    private static UserPayoutInformationResponse requestToExpectedResponse(final UserPayoutInformationRequest userPayoutInformationRequest,
                                                                           final Boolean hasValidContactInfo,
                                                                           final Boolean missingAptosWallet,
                                                                           final Boolean missingEthWallet,
                                                                           final Boolean missingStarknetWallet,
                                                                           final Boolean missingOptimismWallet
    ) {
        return new UserPayoutInformationResponse()
                .company(userPayoutInformationRequest.getCompany())
                .location(userPayoutInformationRequest.getLocation())
                .isCompany(userPayoutInformationRequest.getIsCompany())
                .hasValidContactInfo(hasValidContactInfo)
                .payoutSettings(new UserPayoutInformationResponsePayoutSettings()
                        .aptosAddress(userPayoutInformationRequest.getPayoutSettings().getAptosAddress())
                        .missingAptosWallet(missingAptosWallet)
                        .ethAddress(userPayoutInformationRequest.getPayoutSettings().getEthAddress())
                        .ethName(userPayoutInformationRequest.getPayoutSettings().getEthName())
                        .missingEthWallet(missingEthWallet)
                        .starknetAddress(userPayoutInformationRequest.getPayoutSettings().getStarknetAddress())
                        .missingStarknetWallet(missingStarknetWallet)
                        .optimismAddress(userPayoutInformationRequest.getPayoutSettings().getOptimismAddress())
                        .missingOptimismWallet(missingOptimismWallet)
                        .usdPreferredMethod(switch (userPayoutInformationRequest.getPayoutSettings().getUsdPreferredMethod()) {
                            case FIAT -> UserPayoutInformationResponsePayoutSettings.UsdPreferredMethodEnum.FIAT;
                            case CRYPTO -> UserPayoutInformationResponsePayoutSettings.UsdPreferredMethodEnum.CRYPTO;
                        })
                        .sepaAccount(isNull(userPayoutInformationRequest.getPayoutSettings().getSepaAccount()) ?
                                null :
                                new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                        .bic(userPayoutInformationRequest.getPayoutSettings().getSepaAccount().getBic())
                                        .iban(userPayoutInformationRequest.getPayoutSettings().getSepaAccount().getIban())));
    }
}
