package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import net.minidev.json.JSONArray;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BillingProfileApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    AccountingService accountingService;
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    PayoutPreferenceFacadePort payoutPreferenceFacadePort;

    @Test
    @Order(10)
    void should_be_authenticated() {
        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .bodyValue("""
                        {
                          "name": "test",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(20)
    void should_create_all_type_of_billing_profiles() {
        final String jwt =
                userAuthHelper.newFakeUser(UUID.randomUUID(), faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                        faker.internet().url(), false).jwt();

        /// When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "company",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("company")
                .jsonPath("$.type").isEqualTo("COMPANY")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.kyb.id").isNotEmpty();

        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "company",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("company")
                .jsonPath("$.type").isEqualTo("COMPANY")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(true)
                .jsonPath("$.id").isNotEmpty();


        /// When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "self_employed",
                          "type": "SELF_EMPLOYED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("self_employed")
                .jsonPath("$.type").isEqualTo("SELF_EMPLOYED")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.kyb.id").isNotEmpty();

        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "self_employed",
                          "type": "SELF_EMPLOYED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("self_employed")
                .jsonPath("$.type").isEqualTo("SELF_EMPLOYED")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.kyb.id").isNotEmpty();


        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "individual",
                          "type": "INDIVIDUAL"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("individual")
                .jsonPath("$.type").isEqualTo("INDIVIDUAL")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.kyc.id").isNotEmpty();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "individual",
                          "type": "INDIVIDUAL"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // When
        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[?(@.type == 'SELF_EMPLOYED' && @.name == 'self_employed')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY' && @.name == 'company')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'INDIVIDUAL' && @.name == 'individual')]")
                .value(jsonArray -> Assertions.assertEquals(1, ((JSONArray) jsonArray).toArray().length));

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[?(@.type == 'SELF_EMPLOYED')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')]")
                .value(jsonArray -> Assertions.assertEquals(2, ((JSONArray) jsonArray).toArray().length))
                .jsonPath("$.billingProfiles[?(@.type == 'INDIVIDUAL')]")
                .value(jsonArray -> Assertions.assertEquals(1, ((JSONArray) jsonArray).toArray().length));
    }

    @Test
    @Order(30)
    void should_get_update_delete_billing_profile_by_id() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                faker.internet().url(), false);
        final String jwt =
                authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());
        final CompanyBillingProfile companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);
        final SelfEmployedBillingProfile selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId,
                faker.rickAndMorty().character(), null);
        final IndividualBillingProfile individualBillingProfile = billingProfileService.createIndividualBillingProfile(userId,
                faker.rickAndMorty().character(), null);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(companyBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(companyBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        client.put()
                .uri(BILLING_PROFILES_TYPE_BY_ID.formatted(companyBillingProfile.id().value().toString()))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                        "type": "SELF_EMPLOYED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo("SELF_EMPLOYED")
                .jsonPath("$.status").isEqualTo(companyBillingProfile.status().name())
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        client.put()
                .uri(BILLING_PROFILES_TYPE_BY_ID.formatted(companyBillingProfile.id().value().toString()))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                        "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(companyBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(companyBillingProfile.status().name())
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(null)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(null)
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(selfEmployedBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(selfEmployedBillingProfile.name())
                .jsonPath("$.id").isEqualTo(selfEmployedBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(selfEmployedBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(selfEmployedBillingProfile.status().name())
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(null)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(null)
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(individualBillingProfile.name())
                .jsonPath("$.id").isEqualTo(individualBillingProfile.id().value().toString())
                .jsonPath("$.type").isEqualTo(individualBillingProfile.type().name())
                .jsonPath("$.status").isEqualTo(individualBillingProfile.status().name())
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(5000)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0)
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        client.put()
                .uri(BILLING_PROFILES_TYPE_BY_ID.formatted(individualBillingProfile.id().value().toString()))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                        "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is5xxServerError();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_DELETE_BY_ID.formatted(individualBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    @Order(40)
    void should_put_and_get_billing_profile_payout_infos() {
        // Given
        final var projectId = ProjectId.of("f39b827f-df73-498c-8853-99bc3f562723");
        final var sponsorId = UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d");
        final var pierre = userAuthHelper.authenticatePierre();
        final var authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber(11, true), faker.name().name(),
                faker.internet().url(), false);
        final SelfEmployedBillingProfile selfEmployedBillingProfile =
                billingProfileService.createSelfEmployedBillingProfile(UserId.of(authenticatedUser.user().getId()),
                        faker.rickAndMorty().character(), Set.of(projectId));
        accountingHelper.patchBillingProfile(selfEmployedBillingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": true,
                          "bankAccount": null,
                          "missingBankAccount": false,
                          "ethWallet": null,
                          "missingEthWallet": false,
                          "optimismAddress": null,
                          "missingOptimismWallet": false,
                          "aptosAddress": null,
                          "missingAptosWallet": false,
                          "starknetAddress": null,
                          "missingStarknetWallet": false
                        }""");

        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "ethWallet": "vitalik.eth",
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
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

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": true,
                          "bankAccount": {
                            "bic": "DAAEFRPPCCT",
                            "number": "FR5417569000301995586997O41"
                          },
                          "missingBankAccount": false,
                          "ethWallet": "vitalik.eth",
                          "missingEthWallet": false,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "missingOptimismWallet": false,
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "missingAptosWallet": false,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": false
                        }""");

        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": null,
                          "ethWallet": null,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "bankAccount": null,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": true,
                          "bankAccount": null,
                          "missingBankAccount": false,
                          "ethWallet": null,
                          "missingEthWallet": false,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "missingOptimismWallet": false,
                          "aptosAddress": null,
                          "missingAptosWallet": false,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": false
                        }""");

        // When
        final UUID strkId = currencyRepository.findByCode("STRK").orElseThrow().id();
        final SponsorAccountStatement strk = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(), PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        accountingService.allocate(strk.account().id(), projectId, PositiveAmount.of(100000L), Currency.Id.of(strkId));

        final UUID ethId = currencyRepository.findByCode("ETH").orElseThrow().id();
        final SponsorAccountStatement eth = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(ethId), null,
                new SponsorAccount.Transaction(SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(), PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        accountingService.allocate(eth.account().id(), projectId, PositiveAmount.of(100000L), Currency.Id.of(ethId));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s".formatted(authenticatedUser.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .body(BodyInserters.fromValue(new RewardRequest()
                        .amount(BigDecimal.valueOf(10L))
                        .currencyId(CurrencyHelper.STRK.value())
                        .recipientId(authenticatedUser.user().getGithubUserId())
                        .items(List.of(
                                new RewardItemRequest().id("0011051356")
                                        .type(RewardType.PULL_REQUEST)
                                        .number(1L)
                                        .repoId(55223344L)
                        ))))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s".formatted(authenticatedUser.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .body(BodyInserters.fromValue(new RewardRequest()
                        .amount(BigDecimal.valueOf(20L))
                        .currencyId(CurrencyHelper.ETH.value())
                        .recipientId(authenticatedUser.user().getGithubUserId())
                        .items(List.of(
                                new RewardItemRequest().id("0011051356")
                                        .type(RewardType.PULL_REQUEST)
                                        .number(1L)
                                        .repoId(55223344L)
                        ))))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hasValidPayoutSettings": false,
                          "bankAccount": null,
                          "missingBankAccount": false,
                          "ethWallet": null,
                          "missingEthWallet": true,
                          "optimismAddress": "0x72c30fcd1e7bd691ce206cd36bbd87c4c7099545",
                          "missingOptimismWallet": false,
                          "aptosAddress": null,
                          "missingAptosWallet": false,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": false
                        }""");
    }

    @Test
    @Order(50)
    void should_delete_manually_associated_billing_profile() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var kaaper = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");
        final var firstAdminId = UserId.of(antho.user().getId());

        final var billingProfile = billingProfileService.createCompanyBillingProfile(firstAdminId, faker.rickAndMorty().character(), null);
        payoutPreferenceFacadePort.setPayoutPreference(kaaper, billingProfile.id(), firstAdminId);

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_BY_ID.formatted(billingProfile.id().toString())))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC")
                ))
                .header("Authorization", "Bearer " + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    @Order(60)
    void should_delete_automatically_associated_billing_profile() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var kaaper = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");
        final var firstAdminId = UserId.of(antho.user().getId());

        final var billingProfile = billingProfileService.createCompanyBillingProfile(firstAdminId, faker.rickAndMorty().character(), Set.of(kaaper));

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_BY_ID.formatted(billingProfile.id().toString())))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC")
                ))
                .header("Authorization", "Bearer " + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }
}
