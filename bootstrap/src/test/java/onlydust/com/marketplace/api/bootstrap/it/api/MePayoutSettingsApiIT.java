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


public class MePayoutSettingsApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    private static UserPayoutSettingsResponse requestToExpectedResponse(final UserPayoutSettingsRequest userPayoutInformationRequest,
                                                                        final Boolean missingAptosWallet,
                                                                        final Boolean missingEthWallet,
                                                                        final Boolean missingStarknetWallet,
                                                                        final Boolean missingOptimismWallet
    ) {
        return new UserPayoutSettingsResponse()
                .aptosAddress(userPayoutInformationRequest.getAptosAddress())
                .missingAptosWallet(missingAptosWallet)
                .ethWallet(userPayoutInformationRequest.getEthWallet())
                .missingEthWallet(missingEthWallet)
                .starknetAddress(userPayoutInformationRequest.getStarknetAddress())
                .missingStarknetWallet(missingStarknetWallet)
                .optimismAddress(userPayoutInformationRequest.getOptimismAddress())
                .missingOptimismWallet(missingOptimismWallet)
                .sepaAccount(isNull(userPayoutInformationRequest.getSepaAccount()) ?
                        null :
                        new UserPayoutSettingsResponseSepaAccount()
                                .bic(userPayoutInformationRequest.getSepaAccount().getBic())
                                .iban(userPayoutInformationRequest.getSepaAccount().getIban()));
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
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.ethWallet").isEqualTo("abuisset.eth")
                .jsonPath("$.missingEthWallet").isEqualTo(false)
                .jsonPath("$.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.missingAptosWallet").isEqualTo(true)
                .jsonPath("$.missingStarknetWallet").isEqualTo(true)
                .jsonPath("$.missingSepaAccount").isEqualTo(true)
                .jsonPath("$.hasValidPayoutSettings").isEqualTo(false);
    }

    @Test
    void should_update_user_company_payout_infos() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UserPayoutSettingsRequest requestBody1 = new UserPayoutSettingsRequest();
        requestBody1.aptosAddress("0x" + faker.random().hex(64))
                .sepaAccount(new UserPayoutSettingsResponseSepaAccount()
                        .bic(faker.random().hex())
                        .iban("GB49BARC20037826686157")
                );
        final UserPayoutSettingsRequest requestBody2 = new UserPayoutSettingsRequest();
        requestBody2.ethWallet("0x" + faker.random().hex(40))
                .optimismAddress("0x" + faker.random().hex(40));


        // When
        client.put()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody1))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().equals(requestToExpectedResponse(requestBody1, true, true, true, true));

        client.put()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody2))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().equals(requestToExpectedResponse(requestBody2, true, true, true, true));
    }

    @Test
    void should_update_user_individual_payout_infos() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UserPayoutSettingsRequest requestBody1 = new UserPayoutSettingsRequest();
        requestBody1.aptosAddress("0x" + faker.random().hex(64))
                .sepaAccount(new UserPayoutSettingsResponseSepaAccount()
                        .bic(faker.random().hex())
                        .iban("FR1014508000702139488771C56")
                );
        final UserPayoutSettingsRequest requestBody2 = new UserPayoutSettingsRequest();
        requestBody2.ethWallet("0x" + faker.random().hex(40))
                .optimismAddress("0x" + faker.random().hex(40));


        // When
        client.put()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody1))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().equals(requestToExpectedResponse(requestBody1, true, true, true, true));

        client.put()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(requestBody2))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().equals(requestToExpectedResponse(requestBody2, true, true, true, true));
    }

    @Test
    void should_update_user_individual_payout_infos_with_invalid_iban() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UserPayoutSettingsRequest requestBody = new UserPayoutSettingsRequest();
        requestBody.sepaAccount(new UserPayoutSettingsResponseSepaAccount()
                .bic(faker.random().hex())
                .iban(faker.name().bloodGroup())
        );


        // When
        client.put()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
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
        final UserPayoutSettingsRequest requestBody = new UserPayoutSettingsRequest();
        requestBody.aptosAddress("0x" + faker.random().hex(64))
                .sepaAccount(new UserPayoutSettingsResponseSepaAccount()
                        .bic(faker.random().hex())
                        .iban("FR1014508000702139488771C56")
                );

        // Then
        client.put()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                .header(IMPERSONATION_HEADER, impersonatePierreHeader)
                .body(BodyInserters.fromValue(requestBody))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().equals(requestToExpectedResponse(requestBody, true, true, true, true));

        final String pierreJwt = userAuthHelper.authenticatePierre().jwt();

        client.get()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + pierreJwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .equals(requestToExpectedResponse(requestBody, true, true, true, true));
    }

    @Test
    void should_return_valid_payout_info_given_user_first_connexion() {
        // Given
        final long githubUserId = faker.number().randomNumber();
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), githubUserId,
                faker.rickAndMorty().character(), faker.internet().url(), false).jwt();

        // When
        client.get()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.missingEthWallet").isEqualTo(false)
                .jsonPath("$.missingOptimismWallet").isEqualTo(false)
                .jsonPath("$.missingAptosWallet").isEqualTo(false)
                .jsonPath("$.missingStarknetWallet").isEqualTo(false)
                .jsonPath("$.missingSepaAccount").isEqualTo(false)
                .jsonPath("$.hasValidPayoutSettings").isEqualTo(true);

        // Given
        paymentRequestRepository.save(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId,
                new Date(), BigDecimal.ONE, null, 1, UUID.randomUUID(), CurrencyEnumEntity.op));

        // When
        client.get()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.missingEthWallet").isEqualTo(false)
                .jsonPath("$.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.missingAptosWallet").isEqualTo(false)
                .jsonPath("$.missingStarknetWallet").isEqualTo(false)
                .jsonPath("$.missingSepaAccount").isEqualTo(false)
                .jsonPath("$.hasValidPayoutSettings").isEqualTo(false);

        // Given
        paymentRequestRepository.save(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId,
                new Date(), BigDecimal.ONE, null, 1, UUID.randomUUID(), CurrencyEnumEntity.usd));

        // When
        client.get()
                .uri(getApiURI(ME_PAYOUT_SETTINGS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.missingEthWallet").isEqualTo(false)
                .jsonPath("$.missingOptimismWallet").isEqualTo(true)
                .jsonPath("$.missingAptosWallet").isEqualTo(false)
                .jsonPath("$.missingStarknetWallet").isEqualTo(false)
                .jsonPath("$.missingSepaAccount").isEqualTo(true)
                .jsonPath("$.hasValidPayoutSettings").isEqualTo(false);
    }
}
