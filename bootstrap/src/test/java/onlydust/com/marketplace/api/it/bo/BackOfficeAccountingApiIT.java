package onlydust.com.marketplace.api.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.CreateAccountRequest;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.contract.model.CreateRewardResponse;
import onlydust.com.marketplace.api.helper.SponsorHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.read.repositories.BillingProfileReadRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.model.notification.FundsAllocatedToProgram;
import onlydust.com.marketplace.project.domain.model.notification.FundsUngrantedFromProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.backoffice.api.contract.model.BillingProfileType.INDIVIDUAL;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    static final SponsorId COCA_COLAX = SponsorId.of("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    static final SponsorId REDBULL = SponsorId.of("0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa");

    static final ProjectId BRETZEL = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");
    static final ProjectId KAAPER = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

    @Autowired
    private SponsorAccountRepository sponsorAccountRepository;
    @Autowired
    private AccountBookRepository accountBookRepository;
    @Autowired
    private AccountBookEventRepository accountBookEventRepository;
    @Autowired
    private AllTransactionRepository allTransactionRepository;
    @Autowired
    private CachedAccountBookProvider accountBookProvider;
    @Autowired
    private BillingProfileFacadePort billingProfileFacadePort;
    @Autowired
    private BillingProfileReadRepository billingProfileReadRepository;
    @Autowired
    private PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    @Autowired
    private BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    private KycRepository kycRepository;
    @Autowired
    private KybRepository kybRepository;
    @Autowired
    PdfStoragePort pdfStoragePort;
    @Autowired
    RewardStatusStorage rewardStatusStorage;
    @Autowired
    SponsorHelper sponsorHelper;
    @Autowired
    NotificationPort notificationPort;

    UserAuthHelper.AuthenticatedBackofficeUser camille;
    Program program;
    UserAuthHelper.AuthenticatedUser programLead;

    @BeforeEach
    void setup() {
        accountBookEventRepository.deleteAll();
        allTransactionRepository.deleteAll();
        accountBookRepository.deleteAll();
        sponsorAccountRepository.deleteAll();
        accountBookProvider.evictAll();
        camille = userAuthHelper.authenticateCamille();
        programLead = userAuthHelper.authenticateOlivier();
        program = programHelper.create(sponsorHelper.create().id(), programLead);
    }

    @SneakyThrows
    @Test
    void should_allocate_budget_to_program_and_get_refunded_of_unspent_budget() {
        // When
        final var response = client.post()
                .uri(getApiURI(SPONSORS_BY_ID_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 100
                        }
                        """.formatted(STRK))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        reset(notificationPort);
        accountingHelper.allocate(COCA_COLAX, program.id(), 90, STRK);
        accountingHelper.grant(program.id(), BRETZEL, 80, STRK);
        verify(notificationPort).push(any(), any(FundsAllocatedToProgram.class));

        // When
        client.post()
                .uri(getApiURI(PROGRAMS_BY_ID_UNGRANT.formatted(program.id())))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "projectId": "%s",
                            "amount": 40,
                            "currencyId": "%s"
                        }
                        """.formatted(BRETZEL, STRK))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
        verify(notificationPort).push(any(), any(FundsUngrantedFromProject.class));

        // When
        client.post()
                .uri(getApiURI(SPONSORS_BY_ID_UNALLOCATE.formatted(COCA_COLAX)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "programId": "%s",
                            "amount": 50,
                            "currencyId": "%s"
                        }
                        """.formatted(program.id(), STRK))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        Thread.sleep(200);
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getFundsUngrantedFromProjectEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(programLead.userId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo("ofux")))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Grant returned from project")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("A grant has been returned to you from a project. The " +
                                                                                                 "funds have been credited back to your account. You can " +
                                                                                                 "review the details of this transaction on your dashboard."))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Review transaction details")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-admin.onlydust.com/programs/%s".formatted(program.id()))))
                        .withRequestBody(matchingJsonPath("$.to", equalTo("abuisset@gmail.com")))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Grant returned from project"))));
    }

    @Test
    void should_allocate_budget_to_project_and_pay_rewards_on_ethereum() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
        final var ofux = userAuthHelper.authenticateOlivier();

        client.post()
                .uri(getApiURI(SPONSORS_BY_ID_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue(new CreateAccountRequest()
                        .allowance(BigDecimal.valueOf(100))
                        .currencyId(USDC.value()))
                .exchange()
                .expectStatus()
                .isNoContent();

        // Given
        final var depositId = depositHelper.create(REDBULL, Network.ETHEREUM, USDC, BigDecimal.valueOf(40), Deposit.Status.PENDING);
        accountingHelper.approve(depositId);
        accountingHelper.allocate(REDBULL, program.id(), 100, USDC);
        accountingHelper.grant(program.id(), KAAPER, 100, USDC);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        urlEqualTo("/api/v1/users/%d".formatted(ofux.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        final var rewardId = client.post()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "recipientId": "%d",
                            "amount": 30,
                            "currencyId": "%s",
                            "items": [{
                                "type": "PULL_REQUEST",
                                "id": "1703880973",
                                "number": 325,
                                "repoId": 698096830
                            }]
                        }
                        """.formatted(ofux.user().getGithubUserId(), USDC))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateRewardResponse.class)
                .returnResult().getResponseBody().getId();

        // Then
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(USDC.toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(60)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(30)
        ;

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "ETHEREUM",
                            "reference": "0x14"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .json("""
                        {
                            "message": "Reward %s is not payable on ETHEREUM"
                        }
                        """.formatted(rewardId));

        // When
        invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "ETHEREUM",
                            "reference": "0x14"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + ofux.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "amount": {
                             "amount": 30,
                             "prettyAmount": 30,
                             "currency": {
                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                               "code": "USDC",
                               "name": "USD Coin",
                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                               "decimals": 6
                             },
                             "usdEquivalent": 30.30
                           },
                           "status": "COMPLETE",
                           "from": {
                             "login": "AnthonyBuisset"
                           },
                           "to": {
                             "login": "ofux"
                           },
                           "receipt": {
                             "type": "CRYPTO",
                             "walletAddress": "ofux.eth",
                             "transactionReference": "0x14",
                             "transactionReferenceLink": "https://etherscan.io/tx/0x14"
                           }
                         }
                        """
                );

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(USDC.toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(10)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(60)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(0)
        ;
    }

    @Test
    void should_allocate_budget_to_project_and_pay_rewards_on_starknet() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
        final var ofux = userAuthHelper.authenticateOlivier();

        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "STARKNET",
                            "address": "0x053c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk();

        client.post()
                .uri(getApiURI(SPONSORS_BY_ID_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue(new CreateAccountRequest()
                        .allowance(BigDecimal.valueOf(100))
                        .currencyId(USDC.value()))
                .exchange()
                .expectStatus()
                .isNoContent();

        // Given
        final var depositId = depositHelper.create(REDBULL, Network.STARKNET, USDC, BigDecimal.valueOf(40), Deposit.Status.PENDING);
        accountingHelper.approve(depositId);
        accountingHelper.allocate(REDBULL, program.id(), 100, USDC);
        accountingHelper.grant(program.id(), KAAPER, 100, USDC);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        urlEqualTo("/api/v1/users/%d".formatted(ofux.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        final var rewardId = client.post()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "recipientId": "%d",
                            "amount": 30,
                            "currencyId": "%s",
                            "items": [{
                                "type": "PULL_REQUEST",
                                "id": "1703880973",
                                "number": 325,
                                "repoId": 698096830
                            }]
                        }
                        """.formatted(ofux.user().getGithubUserId(), USDC))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateRewardResponse.class)
                .returnResult().getResponseBody().getId();

        // Then
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(USDC.toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(60)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(30)
        ;

        // When
        invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "STARKNET",
                            "reference": "0x16096a49c236dfdbc5808c31a1d6eee90a082ca5717366a73b03a2eb80cd252"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + ofux.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "amount": {
                             "amount": 30,
                             "prettyAmount": 30,
                             "currency": {
                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                               "code": "USDC",
                               "name": "USD Coin",
                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                               "decimals": 6
                             },
                             "usdEquivalent": 30.30,
                             "usdConversionRate": 1.01
                           },
                           "status": "COMPLETE",
                           "unlockDate": null,
                           "from": {
                             "githubUserId": 43467246,
                             "login": "AnthonyBuisset",
                             "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                             "isRegistered": true
                           },
                           "to": {
                             "githubUserId": 595505,
                             "login": "ofux",
                             "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                             "isRegistered": true
                           },
                           "project": {
                             "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                             "slug": "kaaper",
                             "name": "kaaper",
                             "logoUrl": null,
                             "shortDescription": "Documentation generator for Cairo projects.",
                             "visibility": "PUBLIC"
                           },
                           "receipt": {
                             "type": "CRYPTO",
                             "walletAddress": "0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905",
                             "transactionReference": "0x16096a49c236dfdbc5808c31a1d6eee90a082ca5717366a73b03a2eb80cd252",
                             "transactionReferenceLink": "https://starkscan.co/tx/0x016096a49c236dfdbc5808c31a1d6eee90a082ca5717366a73b03a2eb80cd252"
                           }
                         }
                        """
                );

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(USDC.toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(10)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(60)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(0)
        ;
    }

    @Test
    void should_allocate_budget_to_project_and_pay_rewards_on_aptos() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
        final var ofux = userAuthHelper.authenticateOlivier();

        client.post()
                .uri(getApiURI(SPONSORS_BY_ID_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue(new CreateAccountRequest()
                        .allowance(BigDecimal.valueOf(100))
                        .currencyId(APT.value()))
                .exchange()
                .expectStatus()
                .isNoContent();

        // Given
        final var depositId = depositHelper.create(REDBULL, Network.APTOS, APT, BigDecimal.valueOf(40), Deposit.Status.PENDING);
        accountingHelper.approve(depositId);
        accountingHelper.allocate(REDBULL, program.id(), 100, APT);
        accountingHelper.grant(program.id(), KAAPER, 100, APT);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        urlEqualTo("/api/v1/users/%d".formatted(ofux.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        final var rewardId = client.post()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "recipientId": "%d",
                            "amount": 30,
                            "currencyId": "%s",
                            "items": [{
                                "type": "PULL_REQUEST",
                                "id": "1703880973",
                                "number": 325,
                                "repoId": 698096830
                            }]
                        }
                        """.formatted(ofux.user().getGithubUserId(), APT))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateRewardResponse.class)
                .returnResult().getResponseBody().getId();

        // Then
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(APT.toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(60)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(30)
        ;

        // When
        invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "APTOS",
                            "reference": "0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + ofux.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        
                        {
                           "amount": {
                             "amount": 30,
                             "prettyAmount": 30,
                             "currency": {
                               "code": "APT"
                             }
                           },
                           "status": "COMPLETE",
                           "receipt": {
                             "type": "CRYPTO",
                             "walletAddress": "0x66cb05df2d855fbae92cdb2dfac9a0b29c969a03998fa817735d27391b52b189",
                             "transactionReference": "0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f",
                             "transactionReferenceLink": "https://aptoscan.com/transaction/0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f?network=mainnet"
                           }
                         }
                        """
                );

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(APT.toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(40)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(10)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(60)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(0)
        ;
    }

    @Test
    void should_get_billing_profile() {
        final var billingProfileId = BillingProfile.Id.of("1253b889-e5d5-49ee-8e8a-21405ccab8a6");
        final var kyb = kybRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow();
        kyb.applicantId("123456");
        kybRepository.save(kyb);

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .bankAccount(BankAccount.builder()
                        .bic("AGFBFRCC")
                        .accountNumber("NL50RABO3741207772")
                        .build())
                .ethWallet(Ethereum.wallet("vitalik.eth"))
                .optimismAddress(Optimism.accountAddress("0x1111"))
                .aptosAddress(Aptos.accountAddress("0x2222"))
                .starknetAddress(StarkNet.accountAddress("0x3333"))
                .build(), billingProfileId);

        client.get()
                .uri(getApiURI(BILLING_PROFILE.formatted("1253b889-e5d5-49ee-8e8a-21405ccab8a6")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "1253b889-e5d5-49ee-8e8a-21405ccab8a6",
                          "subject": null,
                          "type": "SELF_EMPLOYED",
                          "verificationStatus": "NOT_STARTED",
                          "kyb": {
                            "name": null,
                            "registrationNumber": null,
                            "registrationDate": null,
                            "address": "19 rue pasteur, 92300, Levallois, France",
                            "country": "France",
                            "countryCode": "FRA",
                            "usEntity": null,
                            "subjectToEuropeVAT": null,
                            "euVATNumber": null,
                            "sumsubUrl": "https://cockpit.sumsub.com/checkus/#/applicant/123456/basicInfo?clientId=onlydust"
                          },
                          "kyc": null,
                          "admins": [
                            {
                              "githubUserId": 31901905,
                              "githubLogin": "kaelsky",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                              "email": "chimansky.mickael@gmail.com",
                              "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274",
                              "name": null
                            }
                          ],
                          "currentMonthRewardedAmounts": [],
                          "payoutInfos": {
                            "bankAccount": {
                              "bic": "AGFBFRCC",
                              "number": "NL50RABO3741207772"
                            },
                            "ethWallet": "vitalik.eth",
                            "optimismAddress": "0x1111",
                            "aptosAddress": "0x2222",
                            "starknetAddress": "0x3333"
                          }
                        }
                        """);
    }

    @Test
    void should_get_current_month_rewarded_amounts() {
        final var billingProfileId = BillingProfile.Id.of("9cae91ac-e70f-426f-af0d-e35c1d3578ed");
        final var kyb = kybRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow();
        kyb.applicantId("123456");
        kybRepository.save(kyb);

        final var rewardIds = List.of(
                RewardId.of("1c56d096-5284-4ae3-af3c-dd2b3211fb73"),
                RewardId.of("4ccf3463-c77d-42cd-85f3-b393901a89c1"),
                RewardId.of("3c9064c2-4513-4876-b5dc-eab38f58f3f1"),
                RewardId.of("b0ceb0cc-294d-49e3-807e-d1a04acea11d")
        );

        rewardIds.forEach(rewardId -> rewardStatusStorage.updatePaidAt(rewardId, ZonedDateTime.now()));


        client.get()
                .uri(getApiURI(BILLING_PROFILE.formatted(billingProfileId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "currentMonthRewardedAmounts": [{
                            "amount": 2000,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            },
                            "dollarsEquivalent": 2000
                          },
                          {
                            "amount": 2000.00,
                            "currency": {
                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "decimals": 6
                            },
                            "dollarsEquivalent": 2020.0000
                          }]
                        }
                        """);
    }

    private Invoice.Id invoiceReward(UserId userId, ProjectId projectId, RewardId rewardId) {
        final var billingProfileId = billingProfileReadRepository.findByUserId(userId.value())
                .stream().filter(bp -> bp.type() == INDIVIDUAL)
                .findFirst()
                .map(billingProfileReadEntity -> BillingProfile.Id.of(billingProfileReadEntity.id()))
                .orElseGet(() -> billingProfileFacadePort.createIndividualBillingProfile(userId, "Personal", null).id());

        final var kyc = kycRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow();

        billingProfileStoragePort.updateBillingProfileStatus(billingProfileId, VerificationStatus.VERIFIED);
        kycRepository.save(kyc.toBuilder()
                .firstName(faker.name().firstName())
                .address(faker.address().fullAddress())
                .consideredUsPersonQuestionnaire(false)
                .idDocumentCountryCode("FRA")
                .country("FRA")
                .build());

        billingProfileFacadePort.updatePayoutInfo(billingProfileId, userId,
                PayoutInfo.builder()
                        .ethWallet(Ethereum.wallet("ofux.eth"))
                        .starknetAddress(StarkNet.accountAddress("0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905"))
                        .aptosAddress(Aptos.accountAddress("0x66cb05df2d855fbae92cdb2dfac9a0b29c969a03998fa817735d27391b52b189"))
                        .build());
        payoutPreferenceFacadePort.setPayoutPreference(projectId, billingProfileId, userId);

        final var invoiceId = billingProfileFacadePort.previewInvoice(userId, billingProfileId, List.of(rewardId)).id();
        final var pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

        when(pdfStoragePort.upload(eq(invoiceId.value() + ".pdf"), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });

        billingProfileFacadePort.uploadGeneratedInvoice(userId, billingProfileId, invoiceId, pdf);

        return invoiceId;
    }
}
