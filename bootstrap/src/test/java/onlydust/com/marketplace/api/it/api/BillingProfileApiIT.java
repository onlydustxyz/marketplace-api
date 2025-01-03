package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import net.minidev.json.JSONArray;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagAccounting
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BillingProfileApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    AccountingService accountingService;
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    @Autowired
    KycRepository kycRepository;

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
                userAuthHelper.signUpUser(faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
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
                .jsonPath("$.id").isNotEmpty();

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
                .jsonPath("$.id").isNotEmpty();

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
                .jsonPath("$.id").isNotEmpty();


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
                .jsonPath("$.id").isNotEmpty();

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
    }

    @Test
    @Order(30)
    void should_get_update_delete_billing_profile_by_id() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
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
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0)
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
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0)
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
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(null)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0)
                .jsonPath("$.me.canDelete").isEqualTo(true)
                .jsonPath("$.me.canLeave").isEqualTo(false)
                .jsonPath("$.me.role").isEqualTo("ADMIN")
                .jsonPath("$.me.invitation").isEmpty();

        // When
        final var kyc = kycRepository.findByBillingProfileId(individualBillingProfile.id().value()).orElseThrow();
        kyc.country("FRA");
        kycRepository.saveAndFlush(kyc);
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(individualBillingProfile.id().value().toString())
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(5001)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0);

        // When
        kyc.country("IND");
        kycRepository.saveAndFlush(kyc);
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(individualBillingProfile.id().value().toString())
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(20001)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0);

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
        final var authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(11, true), faker.name().name(),
                faker.internet().url(), false);
        final SelfEmployedBillingProfile selfEmployedBillingProfile =
                billingProfileService.createSelfEmployedBillingProfile(UserId.of(authenticatedUser.user().getId()),
                        faker.rickAndMorty().character(), Set.of(projectId));
        accountingHelper.patchBillingProfile(selfEmployedBillingProfile.id().value(), null, VerificationStatus.VERIFIED);

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
                          "missingStarknetWallet": false,
                          "stellarAccountId": null,
                          "missingStellarWallet": false
                        }""");

        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
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
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "stellarAccountId": "GA6MC3D6BNEFHZBYROFJ67O6TSZ2JZCDH3Y2PFJUUIDOEX26HDBHD4PB",
                          "nearAccountId": "abuisset.near"
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
                          "optimismAddress": "0x72C30FCD1e7bd691Ce206Cd36BbD87C4C7099545",
                          "missingOptimismWallet": false,
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "missingAptosWallet": false,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": false,
                          "stellarAccountId": "GA6MC3D6BNEFHZBYROFJ67O6TSZ2JZCDH3Y2PFJUUIDOEX26HDBHD4PB",
                          "missingStellarWallet": false,
                          "nearAccountId": "abuisset.near",
                          "missingNearWallet": false
                        }""");

        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(selfEmployedBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": null,
                          "ethWallet": null,
                          "optimismAddress": "0x72C30FCD1e7bd691Ce206Cd36BbD87C4C7099545",
                          "bankAccount": null,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "stellarAccountId": null,
                          "nearAccountId": null
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
                          "optimismAddress": "0x72C30FCD1e7bd691Ce206Cd36BbD87C4C7099545",
                          "missingOptimismWallet": false,
                          "aptosAddress": null,
                          "missingAptosWallet": false,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": false,
                          "stellarAccountId": null,
                          "missingStellarWallet": false,
                          "nearAccountId": null,
                          "missingNearWallet": false
                        }""");

        // When
        final var programId = programHelper.randomId();
        final UUID strkId = currencyRepository.findByCode("STRK").orElseThrow().id();
        accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));
        accountingService.allocate(SponsorId.of(sponsorId), programId, PositiveAmount.of(100000L), Currency.Id.of(strkId));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(strkId));

        final UUID ethId = currencyRepository.findByCode("ETH").orElseThrow().id();
        accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(ethId), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));
        accountingService.allocate(SponsorId.of(sponsorId), programId, PositiveAmount.of(100000L), Currency.Id.of(ethId));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(ethId));

        final UUID usdcId = currencyRepository.findByCode("USDC").orElseThrow().id();
        accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(usdcId), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.STELLAR, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));
        accountingService.allocate(SponsorId.of(sponsorId), programId, PositiveAmount.of(100000L), Currency.Id.of(usdcId));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(usdcId));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s?forceRefresh=false".formatted(authenticatedUser.user().getGithubUserId())))
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
                                new RewardItemRequest().id("1624000021")
                                        .type(RewardType.PULL_REQUEST)
                                        .number(1502L)
                                        .repoId(498695724L)
                        ))))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s?forceRefresh=false".formatted(authenticatedUser.user().getGithubUserId())))
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
                                new RewardItemRequest().id("1624000021")
                                        .type(RewardType.PULL_REQUEST)
                                        .number(1502L)
                                        .repoId(498695724L)
                        ))))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();


        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .body(BodyInserters.fromValue(new RewardRequest()
                        .amount(BigDecimal.valueOf(5000L)) // More than current allowance on ETHEREUM: 400 USDC
                        .currencyId(CurrencyHelper.USDC.value())
                        .recipientId(authenticatedUser.user().getGithubUserId())
                        .items(List.of(
                                new RewardItemRequest().id("1624000021")
                                        .type(RewardType.PULL_REQUEST)
                                        .number(1502L)
                                        .repoId(498695724L)
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
                          "optimismAddress": "0x72C30FCD1e7bd691Ce206Cd36BbD87C4C7099545",
                          "missingOptimismWallet": false,
                          "aptosAddress": null,
                          "missingAptosWallet": false,
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798",
                          "missingStarknetWallet": false,
                          "stellarAccountId": null,
                          "missingStellarWallet": true
                        }""");
    }

    @Test
    @Order(50)
    void should_delete_manually_associated_billing_profile() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
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
        final var antho = userAuthHelper.authenticateAntho();
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

    @Autowired
    CustomerIOProperties customerIOProperties;

    @Test
    void should_send_notifications_to_billing_profile_admins_to_remind_them_to_complete_their_billing_profiles() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser1 = userAuthHelper.create();
        final UserAuthHelper.AuthenticatedUser authenticatedUser2 = userAuthHelper.create();
        final IndividualBillingProfile individual1 = billingProfileService.createIndividualBillingProfile(UserId.of(authenticatedUser1.user().getId()),
                "individual-bp-to-remind-1", Set.of());
        final IndividualBillingProfile individual2 = billingProfileService.createIndividualBillingProfile(UserId.of(authenticatedUser2.user().getId()),
                "individual-bp-to-remind-2", Set.of());
        final CompanyBillingProfile companyBillingProfile1 = billingProfileService.createCompanyBillingProfile(UserId.of(authenticatedUser1.user().getId()),
                "company-bp-to-remind-1", Set.of());
        billingProfileService.inviteCoworker(companyBillingProfile1.id(), UserId.of(authenticatedUser1.user().getId()),
                GithubUserId.of(authenticatedUser2.user().getGithubUserId()), BillingProfile.User.Role.ADMIN);
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile1.id(), GithubUserId.of(authenticatedUser2.user().getGithubUserId()));
        updateBillingProfileCreationDate(individual1.id(), ZonedDateTime.now().minusDays(1));
        updateBillingProfileCreationDate(companyBillingProfile1.id(), ZonedDateTime.now().minusDays(1));


        // When
        billingProfileService.remindUsersToCompleteTheirBillingProfiles();
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageSize", "10", "pageIndex", "0")))
                .header("Authorization", "Bearer " + authenticatedUser1.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_REMINDER",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": {
                                  "billingProfileName": "company-bp-to-remind-1"
                                },
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_REMINDER",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": {
                                  "billingProfileName": "individual-bp-to-remind-1"
                                },
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            }
                          ]
                        }
                        """)
                .jsonPath("$.notifications[0].data.globalBillingProfileReminder.billingProfileId").isNotEmpty()
                .jsonPath("$.notifications[1].data.globalBillingProfileReminder.billingProfileId").isNotEmpty();

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getCompleteYourBillingProfileEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(authenticatedUser1.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(authenticatedUser1.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Complete your billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.hasMoreInformation", equalTo("true")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Resume my billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-app.onlydust.com/settings/billing/%s/general-information".formatted(individual1.id().value()))))
                        .withRequestBody(matchingJsonPath("$.message_data.description",
                                equalTo("You have started the creation of a billing profile individual-bp-to-remind-1, but we need additional information to " +
                                        "validate it.")))

                        .withRequestBody(matchingJsonPath("$.to", equalTo(authenticatedUser1.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Complete your billing profile")))
        );

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getCompleteYourBillingProfileEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(authenticatedUser1.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(authenticatedUser1.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Complete your billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.hasMoreInformation", equalTo("true")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Resume my billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-app.onlydust.com/settings/billing/%s/general-information".formatted(companyBillingProfile1.id().value()))))
                        .withRequestBody(matchingJsonPath("$.message_data.description",
                                equalTo("You have started the creation of a billing profile company-bp-to-remind-1, but we need additional information to " +
                                        "validate it.")))

                        .withRequestBody(matchingJsonPath("$.to", equalTo(authenticatedUser1.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Complete your billing profile")))
        );

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getCompleteYourBillingProfileEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(authenticatedUser2.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(authenticatedUser2.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Complete your billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.hasMoreInformation", equalTo("true")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Resume my billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-app.onlydust.com/settings/billing/%s/general-information".formatted(companyBillingProfile1.id().value()))))
                        .withRequestBody(matchingJsonPath("$.message_data.description",
                                equalTo("You have started the creation of a billing profile company-bp-to-remind-1, but we need additional information to " +
                                        "validate it.")))

                        .withRequestBody(matchingJsonPath("$.to", equalTo(authenticatedUser2.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Complete your billing profile")))
        );


        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageSize", "10", "pageIndex", "0")))
                .header("Authorization", "Bearer " + authenticatedUser2.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "totalPageNumber": 1,
                           "totalItemNumber": 1,
                           "hasMore": false,
                           "nextPageIndex": 0,
                           "notifications": [
                             {
                               "status": "UNREAD",
                               "type": "GLOBAL_BILLING_PROFILE_REMINDER",
                               "data": {
                                 "maintainerApplicationToReview": null,
                                 "maintainerCommitteeApplicationCreated": null,
                                 "contributorInvoiceRejected": null,
                                 "contributorRewardCanceled": null,
                                 "contributorRewardReceived": null,
                                 "contributorRewardsPaid": null,
                                 "contributorProjectApplicationAccepted": null,
                                 "globalBillingProfileReminder": {
                                   "billingProfileName": "company-bp-to-remind-1"
                                 },
                                 "globalBillingProfileVerificationRejected": null,
                                 "globalBillingProfileVerificationClosed": null
                               }
                             }
                           ]
                         }
                        """);

        updateBillingProfileCreationDate(individual2.id(), ZonedDateTime.now().minusDays(7));
        updateBillingProfileCreationDate(companyBillingProfile1.id(), ZonedDateTime.now().minusDays(2));
        billingProfileService.remindUsersToCompleteTheirBillingProfiles();

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageSize", "10", "pageIndex", "0")))
                .header("Authorization", "Bearer " + authenticatedUser2.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_REMINDER",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                 "globalBillingProfileVerificationRejected": null,
                                 "globalBillingProfileVerificationClosed": null,
                                "globalBillingProfileReminder": {
                                  "billingProfileName": "individual-bp-to-remind-2"
                                }
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_REMINDER",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                 "globalBillingProfileVerificationRejected": null,
                                 "globalBillingProfileVerificationClosed": null,
                                "globalBillingProfileReminder": {
                                  "billingProfileName": "company-bp-to-remind-1"
                                }
                              }
                            }
                          ]
                        }
                        """);

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getCompleteYourBillingProfileEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(authenticatedUser2.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(authenticatedUser2.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Complete your billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.hasMoreInformation", equalTo("true")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Resume my billing profile")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-app.onlydust.com/settings/billing/%s/general-information".formatted(individual2.id().value()))))
                        .withRequestBody(matchingJsonPath("$.message_data.description",
                                equalTo("You have started the creation of a billing profile individual-bp-to-remind-2, but we need additional information to " +
                                        "validate it.")))

                        .withRequestBody(matchingJsonPath("$.to", equalTo(authenticatedUser2.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Complete your billing profile")))
        );
    }


    @Autowired
    EntityManagerFactory entityManagerFactory;

    private void updateBillingProfileCreationDate(final BillingProfile.Id billingProfileId, final ZonedDateTime creationDate) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("update accounting.billing_profiles set tech_created_at = :creationDate where id = :billingProfileId")
                .setParameter("creationDate", creationDate)
                .setParameter("billingProfileId", billingProfileId.value())
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
}
