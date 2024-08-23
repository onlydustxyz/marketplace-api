package onlydust.com.marketplace.api.it.api.feature;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.helper.AccountingHelper;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

public class AlertingIT extends AbstractMarketplaceApiIT {
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    AccountingService accountingService;
    @Autowired
    AccountingHelper accountingHelper;

    @Test
    void should_return_proper_alerting_data() {
        // Given
        final var authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(11, true), "another-fake-user",
                faker.internet().url(), false);

        // When user has no BP and no reward
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of())
                .build()), 0);

        // When user has a not-verified BP and no reward
        final var individualBillingProfile = billingProfileService.createIndividualBillingProfile(UserId.of(authenticatedUser.user().getId()),
                faker.rickAndMorty().character(), null);

        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("NOT_STARTED")
                        .missingPayoutInfo(false)
                        .missingVerification(false)
                        .verificationBlocked(false)
                        .individualLimitReached(false)
                        .missingEthWallet(false)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 0);

        // When user has a not-verified BP and some reward
        final var projectId = ProjectId.of("f39b827f-df73-498c-8853-99bc3f562723");
        final var sponsorId = UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d");
        final var usdc = currencyRepository.findByCode("USDC").orElseThrow().id();
        accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(usdc), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        final var programId = ProgramId.random();
        accountingService.allocate(SponsorId.of(sponsorId), programId, PositiveAmount.of(100000L), Currency.Id.of(usdc));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(usdc));
        sendRewardToRecipient(authenticatedUser.user().getGithubUserId(), 100L, projectId.value());
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(true)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("NOT_STARTED")
                        .missingPayoutInfo(false)
                        .missingVerification(false)
                        .verificationBlocked(false)
                        .individualLimitReached(false)
                        .missingEthWallet(false)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 1);

        // When the user set missing payout preferences
        updatePayoutPreferences(authenticatedUser.user().getGithubUserId(), individualBillingProfile.id(), projectId.value());
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("NOT_STARTED")
                        .missingPayoutInfo(false)
                        .missingVerification(true)
                        .verificationBlocked(false)
                        .individualLimitReached(false)
                        .missingEthWallet(false)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 1);

        // When the user gets his BP verification blocked
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatus.CLOSED);
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("CLOSED")
                        .missingPayoutInfo(false)
                        .missingVerification(true)
                        .verificationBlocked(true)
                        .individualLimitReached(false)
                        .missingEthWallet(false)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 1);

        // When the user gets his BP verified
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatus.VERIFIED);
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("VERIFIED")
                        .missingPayoutInfo(true)
                        .missingVerification(false)
                        .verificationBlocked(false)
                        .individualLimitReached(false)
                        .missingEthWallet(true)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 1);

        // When the user adds some payout infos
        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(individualBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "ethWallet": "vitalik.eth",
                          "optimismAddress": "0x72C30FCD1e7bd691Ce206Cd36BbD87C4C7099545",
                          "bankAccount": {
                            "bic": "DAAEFRPPCCT",
                            "number": "FR5417569000301995586997O41"
                          },
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("VERIFIED")
                        .missingPayoutInfo(false)
                        .missingVerification(false)
                        .verificationBlocked(false)
                        .individualLimitReached(false)
                        .missingEthWallet(false)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 1);


        sendRewardToRecipient(authenticatedUser.user().getGithubUserId(), 6000L, projectId.value());
        assertAlerting(List.of(MeDatum.builder()
                .githubUserId(authenticatedUser.user().getGithubUserId())
                .missingPayoutPreference(false)
                .billingProfileData(List.of(MeBillingProfileDatum.builder()
                        .billingProfileId(individualBillingProfile.id().value())
                        .status("VERIFIED")
                        .missingPayoutInfo(false)
                        .missingVerification(false)
                        .verificationBlocked(false)
                        .individualLimitReached(true)
                        .missingEthWallet(false)
                        .missingAptosWallet(false)
                        .missingOptimismWallet(false)
                        .missingStarknetWallet(false)
                        .missingBankAccount(false)
                        .build()))
                .build()), 1);
    }


    private void assertAlerting(final List<MeDatum> meData, final int expectedPayoutPreferenceCount) {
        for (final var meDatum : meData) {
            final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(meDatum.githubUserId);

            {
                final WebTestClient.BodyContentSpec bodyContentSpec = client.get()
                        .uri(getApiURI(ME))
                        .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                        // Then
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .jsonPath("$.missingPayoutPreference").isEqualTo(meDatum.missingPayoutPreference);
            }
            {
                for (final var bpDatum : meDatum.billingProfileData()) {
                    client.get()
                            .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(bpDatum.billingProfileId)))
                            .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                            // Then
                            .exchange()
                            .expectStatus()
                            .is2xxSuccessful()
                            .expectBody()
                            .jsonPath("$.id").isEqualTo(bpDatum.billingProfileId.toString())
                            .jsonPath("$.status").isEqualTo(bpDatum.status)
                            .jsonPath("$.missingPayoutInfo").isEqualTo(bpDatum.missingPayoutInfo)
                            .jsonPath("$.missingVerification").isEqualTo(bpDatum.missingVerification)
                            .jsonPath("$.verificationBlocked").isEqualTo(bpDatum.verificationBlocked)
                            .jsonPath("$.individualLimitReached").isEqualTo(bpDatum.individualLimitReached);

                    client.get()
                            .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(bpDatum.billingProfileId)))
                            .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                            // Then
                            .exchange()
                            .expectStatus()
                            .is2xxSuccessful()
                            .expectBody()
                            .jsonPath("$.missingBankAccount").isEqualTo(bpDatum.missingBankAccount)
                            .jsonPath("$.missingEthWallet").isEqualTo(bpDatum.missingEthWallet)
                            .jsonPath("$.missingOptimismWallet").isEqualTo(bpDatum.missingOptimismWallet)
                            .jsonPath("$.missingAptosWallet").isEqualTo(bpDatum.missingAptosWallet)
                            .jsonPath("$.missingStarknetWallet").isEqualTo(bpDatum.missingStarknetWallet);
                }
            }
            {
                final WebTestClient.BodyContentSpec bodyContentSpec = client.get()
                        .uri(getApiURI(ME_BILLING_PROFILES))
                        .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                        // Then
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .jsonPath("$.billingProfiles.length()").isEqualTo(meDatum.billingProfileData.size());

                for (final var bpDatum : meDatum.billingProfileData()) {
                    bodyContentSpec
                            .jsonPath("$.billingProfiles[?(@.id == '%s')].missingPayoutInfo".formatted(bpDatum.billingProfileId.toString()))
                            .isEqualTo(bpDatum.missingPayoutInfo);
                    bodyContentSpec
                            .jsonPath("$.billingProfiles[?(@.id == '%s')].missingVerification".formatted(bpDatum.billingProfileId.toString()))
                            .isEqualTo(bpDatum.missingVerification);
                    bodyContentSpec
                            .jsonPath("$.billingProfiles[?(@.id == '%s')].verificationBlocked".formatted(bpDatum.billingProfileId.toString()))
                            .isEqualTo(bpDatum.verificationBlocked);
                    bodyContentSpec
                            .jsonPath("$.billingProfiles[?(@.id == '%s')].individualLimitReached".formatted(bpDatum.billingProfileId.toString()))
                            .isEqualTo(bpDatum.individualLimitReached);
                }
            }
            if (!meDatum.missingPayoutPreference()) {
                final WebTestClient.BodyContentSpec bodyContentSpec = client.get()
                        .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                        .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                        // Then
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .jsonPath("$.length()").isEqualTo(expectedPayoutPreferenceCount);

                if (expectedPayoutPreferenceCount > 0) {
                    for (final var bpDatum : meDatum.billingProfileData()) {
                        bodyContentSpec
                                .jsonPath("$[?(@.billingProfile.id == '%s')].billingProfile.missingPayoutInfo".formatted(bpDatum.billingProfileId.toString()))
                                .isEqualTo(bpDatum.missingPayoutInfo);
                        bodyContentSpec
                                .jsonPath("$[?(@.billingProfile.id == '%s')].billingProfile.missingVerification".formatted(bpDatum.billingProfileId.toString()))
                                .isEqualTo(bpDatum.missingVerification);
                        bodyContentSpec
                                .jsonPath("$[?(@.billingProfile.id == '%s')].billingProfile.verificationBlocked".formatted(bpDatum.billingProfileId.toString()))
                                .isEqualTo(bpDatum.verificationBlocked);
                        bodyContentSpec
                                .jsonPath("$[?(@.billingProfile.id == '%s')].billingProfile.individualLimitReached".formatted(bpDatum.billingProfileId.toString()))
                                .isEqualTo(bpDatum.individualLimitReached);
                    }
                }
            }

        }
    }

    @Builder
    private record MeDatum(@NonNull Long githubUserId, boolean missingPayoutPreference,
                           @NonNull List<MeBillingProfileDatum> billingProfileData) {
    }

    @Builder
    private record MeBillingProfileDatum(@NonNull UUID billingProfileId, @NonNull String status,
                                         boolean missingPayoutInfo,
                                         boolean missingVerification,
                                         boolean verificationBlocked,
                                         boolean individualLimitReached,
                                         boolean missingBankAccount,
                                         boolean missingEthWallet,
                                         boolean missingOptimismWallet,
                                         boolean missingAptosWallet,
                                         boolean missingStarknetWallet) {
    }

    private void sendRewardToRecipient(Long recipientId, Long amount, UUID projectId) {
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(amount))
                .currencyId(CurrencyHelper.USDC.value())
                .recipientId(recipientId)
                .items(List.of(
                        new RewardItemRequest().id("0011051356")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(55223344L)
                ));

        // When
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s".formatted(recipientId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    private void updatePayoutPreferences(final Long githubUserId, BillingProfile.Id billingProfileId, final UUID projectId) {
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(githubUserId);
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(isNull(billingProfileId) ? null : billingProfileId.value(), projectId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}
