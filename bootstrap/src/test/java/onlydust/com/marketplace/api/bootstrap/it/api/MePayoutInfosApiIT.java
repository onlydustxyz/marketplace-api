package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;


public class MePayoutInfosApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    PaymentRequestRepository paymentRequestRepository;

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
                        .ethWallet(userPayoutInformationRequest.getPayoutSettings().getEthWallet())
                        .missingEthWallet(missingEthWallet)
                        .starknetAddress(userPayoutInformationRequest.getPayoutSettings().getStarknetAddress())
                        .missingStarknetWallet(missingStarknetWallet)
                        .optimismAddress(userPayoutInformationRequest.getPayoutSettings().getOptimismAddress())
                        .missingOptimismWallet(missingOptimismWallet)
                        .sepaAccount(isNull(userPayoutInformationRequest.getPayoutSettings().getSepaAccount()) ?
                                null :
                                new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                        .bic(userPayoutInformationRequest.getPayoutSettings().getSepaAccount().getBic())
                                        .iban(userPayoutInformationRequest.getPayoutSettings().getSepaAccount().getIban())));
    }

    @Test
    void should_get_user_payout_info() {
        // Given
        final UserAuthHelper.AuthenticatedUser anthony = userAuthHelper.authenticateAnthony();
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
                                .currency(CurrencyEnumEntity.strk)
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
                .jsonPath("$.hasValidContactInfo").isEqualTo(true)
                .jsonPath("$.person.lastname").isEqualTo("BUISSET")
                .jsonPath("$.person.firstname").isEqualTo("Anthony")
                .jsonPath("$.location.address").isEqualTo("771 chemin de la sine")
                .jsonPath("$.location.city").isEqualTo("Vence")
                .jsonPath("$.location.country").isEqualTo("France")
                .jsonPath("$.location.postalCode").isEqualTo("06140")
                .jsonPath("$.payoutSettings.ethWallet").isEqualTo("abuisset.eth")
                .jsonPath("$.payoutSettings.missingEthWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingAptosWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingStarknetWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingSepaAccount").isEqualTo(true)
                .jsonPath("$.payoutSettings.hasValidPayoutSettings").isEqualTo(false);
    }

    @Test
    void should_update_user_company_payout_infos() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
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
                        .aptosAddress("0x" + faker.random().hex(64))
                        .sepaAccount(new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban("GB49BARC20037826686157")
                        )
                );
        final UserPayoutInformationRequest requestBody2 = new UserPayoutInformationRequest();
        requestBody2.company(
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
                        .ethWallet("0x" + faker.random().hex(40))
                        .optimismAddress("0x" + faker.random().hex(40))
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
    void should_update_user_individual_payout_infos() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UserPayoutInformationRequest requestBody1 = new UserPayoutInformationRequest();
        requestBody1.person(new PersonIdentity()
                        .firstname(faker.name().firstName())
                        .lastname(faker.name().lastName())
                )
                .isCompany(false)
                .location(new UserPayoutInformationResponseLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationRequestPayoutSettings()
                        .aptosAddress("0x" + faker.random().hex(64))
                        .sepaAccount(new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban("FR1014508000702139488771C56")
                        )
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
                        .ethWallet("0x" + faker.random().hex(40))
                        .optimismAddress("0x" + faker.random().hex(40))
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
    void should_update_user_individual_payout_infos_with_invalid_iban() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UserPayoutInformationRequest requestBody = new UserPayoutInformationRequest();
        requestBody.person(new PersonIdentity()
                        .firstname(faker.name().firstName())
                        .lastname(faker.name().lastName())
                )
                .isCompany(false)
                .location(new UserPayoutInformationResponseLocation()
                        .address(faker.address().fullAddress())
                        .city(faker.address().city())
                        .country(faker.address().country())
                        .postalCode(faker.address().zipCode())
                )
                .payoutSettings(new UserPayoutInformationRequestPayoutSettings()
                        .sepaAccount(new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban(faker.name().bloodGroup())
                        )
                );


        // When
        client.put()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody))
                // Then
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void should_update_user_payout_info_given_impersonate_user() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), githubUserId, faker.rickAndMorty().character(),
                faker.internet().url(), true).jwt();
        userAuthHelper.authenticateUser(githubUserId);
        final String impersonatePierreHeader =
                userAuthHelper.getImpersonationHeaderToImpersonatePierre();

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
                        .aptosAddress("0x" + faker.random().hex(64))
                        .sepaAccount(new UserPayoutInformationResponsePayoutSettingsSepaAccount()
                                .bic(faker.random().hex())
                                .iban("FR1014508000702139488771C56")
                        )
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

        final String pierreJwt = userAuthHelper.authenticatePierre().jwt();

        client.get()
                .uri(getApiURI(ME_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + pierreJwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .equals(requestToExpectedResponse(requestBody, true, true, true, true, true));
    }

    @Test
    void should_return_valid_payout_info_given_user_first_connexion() {
        // Given
        final long githubUserId = faker.number().randomNumber();
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), githubUserId,
                faker.rickAndMorty().character(), faker.internet().url(), false).jwt();

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
                .jsonPath("$.hasValidContactInfo").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingEthWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingOptimismWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingAptosWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingStarknetWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingSepaAccount").isEqualTo(false)
                .jsonPath("$.payoutSettings.hasValidPayoutSettings").isEqualTo(true);

        // Given
        paymentRequestRepository.save(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId,
                new Date(), BigDecimal.ONE, null, 1, UUID.randomUUID(), CurrencyEnumEntity.op));

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
                .jsonPath("$.hasValidContactInfo").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingEthWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingAptosWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingStarknetWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingSepaAccount").isEqualTo(false)
                .jsonPath("$.payoutSettings.hasValidPayoutSettings").isEqualTo(false);

        // Given
        paymentRequestRepository.save(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId,
                new Date(), BigDecimal.ONE, null, 1, UUID.randomUUID(), CurrencyEnumEntity.usd));

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
                .jsonPath("$.hasValidContactInfo").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingEthWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.payoutSettings.missingAptosWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingStarknetWallet").isEqualTo(false)
                .jsonPath("$.payoutSettings.missingSepaAccount").isEqualTo(true)
                .jsonPath("$.payoutSettings.hasValidPayoutSettings").isEqualTo(false);
    }
}
