package onlydust.com.marketplace.api.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.backoffice.api.contract.model.PayRewardRequest;
import onlydust.com.backoffice.api.contract.model.RewardPageResponse;
import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.accounting.domain.service.PayoutPreferenceService;
import onlydust.com.marketplace.api.helper.AccountingHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.read.repositories.BillingProfileReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeAccountingManagementRestApi;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.*;

import static java.util.Objects.isNull;
import static onlydust.com.backoffice.api.contract.model.BillingProfileType.COMPANY;
import static onlydust.com.backoffice.api.contract.model.BillingProfileType.SELF_EMPLOYED;
import static onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT.ME_PUT_PAYOUT_PREFERENCES;
import static org.assertj.core.api.Assertions.assertThat;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeRewardApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    BillingProfileReadRepository billingProfileReadRepository;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    PayoutPreferenceService payoutPreferenceService;
    @Autowired
    BackofficeAccountingManagementRestApi backofficeAccountingManagementRestApi;
    private final Faker faker = new Faker();
    UserAuthHelper.AuthenticatedBackofficeUser camille;

    UserId anthony;
    UserId olivier;
    UserId pierre;
    CompanyBillingProfile olivierBillingProfile;
    SelfEmployedBillingProfile anthonyBillingProfile;
    IndividualBillingProfile pierreBillingProfile;

    static final List<Invoice.Id> anthonyInvoiceIds = new ArrayList<>();
    static final List<Invoice.Id> olivierInvoiceIds = new ArrayList<>();
    static final List<Invoice.Id> pierreInvoiceIds = new ArrayList<>();

    void setUp() throws IOException {
        // Given
        this.anthony = UserId.of(userAuthHelper.authenticateAntho().user().getId());
        this.olivier = UserId.of(userAuthHelper.authenticateOlivier().user().getId());
        this.pierre = UserId.of(userAuthHelper.authenticatePierre().user().getId());

        olivierBillingProfile = billingProfileService.createCompanyBillingProfile(this.olivier, "Apple Inc.", null);
        billingProfileService.updatePayoutInfo(olivierBillingProfile.id(), this.olivier,
                PayoutInfo.builder().bankAccount(new BankAccount("BOURS123", "FR7600111222333444")).build());
        accountingHelper.patchBillingProfile(olivierBillingProfile.id().value(), null, VerificationStatus.VERIFIED);

        anthonyBillingProfile = billingProfileService.createSelfEmployedBillingProfile(this.anthony, "Olivier SASU", null);
        billingProfileService.updatePayoutInfo(anthonyBillingProfile.id(), this.anthony,
                PayoutInfo.builder()
                        .ethWallet(new WalletLocator(new Name(this.anthony + ".eth")))
                        .bankAccount(new BankAccount("BNPAFRPPXXX", "FR7630004000031234567890143"))
                        .build());
        accountingHelper.patchBillingProfile(anthonyBillingProfile.id().value(), null, VerificationStatus.VERIFIED);

        pierreBillingProfile = billingProfileService.createIndividualBillingProfile(this.pierre, "Olivier", null);
        billingProfileService.updatePayoutInfo(pierreBillingProfile.id(), this.pierre,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.pierre + ".eth"))).build());
        accountingHelper.patchBillingProfile(pierreBillingProfile.id().value(), null, VerificationStatus.VERIFIED);

        kybRepository.findByBillingProfileId(olivierBillingProfile.id().value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("1 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("Olivier Inc.")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatus.VERIFIED).build()));
        kybRepository.findByBillingProfileId(anthonyBillingProfile.id().value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("2 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR0987654321")
                        .name("Antho SASU")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("ABC123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatus.VERIFIED).build()));
        kycRepository.findByBillingProfileId(pierreBillingProfile.id().value())
                .ifPresent(kyc -> kycRepository.save(kyc.toBuilder()
                        .country("FRA")
                        .address("3 Infinite Loop, Cupertino, CA 95014, United States")
                        .firstName("Pierre")
                        .lastName("Qui roule n'amasse pas mousses")
                        .birthdate(faker.date().birthday())
                        .consideredUsPersonQuestionnaire(false)
                        .idDocumentCountryCode("FRA")
                        .usCitizen(false)
                        .verificationStatus(VerificationStatus.VERIFIED).build()));

        updatePayoutPreferences(595505L, olivierBillingProfile.id(), UUID.fromString("e41f44a2-464c-4c96-817f-81acb06b2523"));
        updatePayoutPreferences(43467246L, anthonyBillingProfile.id(), UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39"));
        updatePayoutPreferences(16590657L, pierreBillingProfile.id(), UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"));

        // Given
        newOlivierInvoiceToReview(List.of(
                RewardId.of("5c668b61-e42c-4f0e-b31f-44c4e50dc2f4")));
        newOlivierInvoiceToReview(List.of(
                RewardId.of("1fad9f3b-67ab-4499-a320-d719a986d933"),
                RewardId.of("cdea7e15-a757-4aa1-a209-a0a535e9af94")));

        newAnthonyInvoiceToReview(List.of(
                RewardId.of("6587511b-3791-47c6-8430-8f793606c63a"),
                RewardId.of("79209029-c488-4284-aa3f-bce8870d3a66"),
                RewardId.of("303f26b1-63f0-41f1-ab11-e70b54ef4a2a")));
        newAnthonyInvoiceToReview(List.of(
                RewardId.of("0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf"),
                RewardId.of("335e45a5-7f59-4519-8a12-1addc530214c"),
                RewardId.of("e9ebbe59-fb74-4a6c-9a51-6d9050412977")));

        final var pierreRewardIds = List.of(
                RewardId.of("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"),
                RewardId.of("5b96ca1e-4ad2-41c1-8819-520b885d9223"),
                RewardId.of("2ac80cc6-7e83-4eef-bc0c-932b58f683c0"),
                RewardId.of("85f8358c-5339-42ac-a577-16d7760d1e28"),
                RewardId.of("40fda3c6-2a3f-4cdd-ba12-0499dd232d53"));
        newPierreInvoiceToReview(pierreRewardIds);
        pierreRewardIds.forEach(rewardId -> {
            backofficeAccountingManagementRestApi.payReward(rewardId.value(),
                    new PayRewardRequest()
                            .network(TransactionNetwork.ETHEREUM)
                            .reference("0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156"));
        });
    }

    @BeforeEach
    void login() {
        camille = userAuthHelper.authenticateCamille();
    }

    private void newOlivierInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(olivier, olivierBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(olivier, olivierBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        olivierInvoiceIds.add(invoiceId);
    }

    private void newAnthonyInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(anthony, anthonyBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(anthony, anthonyBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        anthonyInvoiceIds.add(invoiceId);
    }

    private void newPierreInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(pierre, pierreBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadGeneratedInvoice(pierre, pierreBillingProfile.id(), invoiceId,
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        pierreInvoiceIds.add(invoiceId);
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

    @Test
    @Order(1)
    void should_get_all_rewards() {
        final var gregoire = UserId.of(userAuthHelper.authenticateGregoire().user().getId());
        final var onlyDustBillingProfileId = BillingProfile.Id.of("9cae91ac-e70f-426f-af0d-e35c1d3578ed");

        billingProfileService.updatePayoutInfo(onlyDustBillingProfileId, gregoire,
                PayoutInfo.builder()
                        .ethWallet(new WalletLocator(new Name(gregoire + ".eth")))
                        .bankAccount(new BankAccount("BNPAFRPPXXX", "FR7630004000031234567890143"))
                        .build());

        kybRepository.findByBillingProfileId(onlyDustBillingProfileId.value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("66 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("OnlyDust")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(false)
                        .verificationStatus(VerificationStatus.VERIFIED).build()));

        accountingHelper.patchBillingProfile(onlyDustBillingProfileId.value(), null, VerificationStatus.VERIFIED);

        billingProfileService.previewInvoice(gregoire, onlyDustBillingProfileId, List.of(RewardId.of("5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c"))).id();

        // When
        client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 49,
                          "totalItemNumber": 244,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "rewards": [
                            {
                              "id": "5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c",
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "status": "PENDING_REQUEST",
                              "money": {
                                "amount": 1000.00,
                                "currency": {
                                  "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                  "code": "USD",
                                  "name": "US Dollar",
                                  "logoUrl": null,
                                  "decimals": 2
                                },
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.00000000000000000000
                              },
                              "recipient": {
                                "githubUserId": 8642470,
                                "userId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "billingProfile": null,
                              "requestedAt": "2023-10-08T10:09:31.842Z",
                              "invoice": null
                            },
                            {
                              "id": "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "status": "PENDING_REQUEST",
                              "money": {
                                "amount": 1000.00,
                                "currency": {
                                  "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                  "code": "USD",
                                  "name": "US Dollar",
                                  "logoUrl": null,
                                  "decimals": 2
                                },
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.00000000000000000000
                              },
                              "recipient": {
                                "githubUserId": 8642470,
                                "userId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "billingProfile": null,
                              "requestedAt": "2023-10-08T10:06:42.73Z",
                              "invoice": null
                            },
                            {
                              "id": "64fb2732-5632-4b09-a8b1-217485648129",
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "status": "PENDING_REQUEST",
                              "money": {
                                "amount": 1000.00,
                                "currency": {
                                  "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                  "code": "USD",
                                  "name": "US Dollar",
                                  "logoUrl": null,
                                  "decimals": 2
                                },
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.00000000000000000000
                              },
                              "recipient": {
                                "githubUserId": 8642470,
                                "userId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "billingProfile": null,
                              "requestedAt": "2023-10-08T10:00:31.105Z",
                              "invoice": null
                            },
                            {
                              "id": "736e0554-f30e-4315-9731-7611fa089dcf",
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "status": "COMPLETE",
                              "money": {
                                "amount": 1000.00,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 1010.0000,
                                "conversionRate": 1.0100000000000000
                              },
                              "recipient": {
                                "githubUserId": 8642470,
                                "userId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "billingProfile": null,
                              "requestedAt": "2023-09-26T15:57:29.834Z",
                              "invoice": null
                            },
                            {
                              "id": "1c56d096-5284-4ae3-af3c-dd2b3211fb73",
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "status": "COMPLETE",
                              "money": {
                                "amount": 1000.00,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 1010.0000,
                                "conversionRate": 1.0100000000000000
                              },
                              "recipient": {
                                "githubUserId": 8642470,
                                "userId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "billingProfile": null,
                              "requestedAt": "2023-09-26T08:43:36.823Z",
                              "invoice": null
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(2)
    void should_get_reward_by_id() throws IOException {
        setUp();

        // When
        client.get()
                .uri(getApiURI(BO_REWARD.formatted("5c668b61-e42c-4f0e-b31f-44c4e50dc2f4")))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "5c668b61-e42c-4f0e-b31f-44c4e50dc2f4",
                          "paymentId": null,
                          "billingProfile": {
                            "subject": "Olivier Inc.",
                            "type": "COMPANY"
                          },
                          "requestedAt": "2023-03-20T12:33:11.124Z",
                          "processedAt": null,
                          "githubUrls": [
                            "https://github.com/onlydustxyz/marketplace-frontend/pull/818"
                          ],
                          "status": "PROCESSING",
                          "project": {
                            "name": "Zero title 5",
                            "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png"
                          },
                          "sponsors": [
                            {
                              "id": "01bc5c57-9b7c-4521-b7be-8a12861ae5f4",
                              "name": "No Sponsor",
                              "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                            }
                          ],
                          "money": {
                            "amount": 1250,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            },
                            "dollarsEquivalent": 1250,
                            "conversionRate": 1.00000000000000000000
                          },
                          "invoiceId": "%s",
                          "receipts": [],
                          "pendingPayments": [
                            {
                              "network": "SEPA",
                              "billingAccountNumber": "FR7600111222333444",
                              "amount": 1250
                            }
                          ]
                        }
                        """.formatted(olivierInvoiceIds.get(0).value()));

        // When
        client.get()
                .uri(getApiURI(BO_REWARD.formatted("fab7aaf4-9b0c-4e52-bc9b-72ce08131617")))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                          "project": {
                            "name": "Bretzel",
                            "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                          },
                          "status": "PENDING_REQUEST",
                          "money": {
                            "amount": 1000.00,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            },
                            "dollarsEquivalent": 1000.00,
                            "conversionRate": 1.00000000000000000000
                          },
                          "invoiceId": null,
                          "receipts": [],
                          "pendingPayments": []
                        }
                        """);
    }

    @Test
    @Order(3)
    void should_get_all_rewards_with_status() {

        // When
        client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5", "statuses", "PENDING_VERIFICATION")))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.status == 'PENDING_VERIFICATION')]").isArray()
                .jsonPath("$.rewards[?(@.status != 'PENDING_VERIFICATION')]").doesNotExist();
    }

    @Test
    @Order(3)
    void should_get_all_rewards_with_billing_profile_id() {
        // Given
        anthony = UserId.of(userAuthHelper.authenticateAntho().user().getId());
        final var billingProfile = billingProfileReadRepository.findByUserId(anthony.value()).stream()
                .filter(b -> b.type() == SELF_EMPLOYED)
                .findFirst().orElseThrow();

        // When
        final var rewards = client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5", "billingProfiles", billingProfile.id().toString())))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RewardPageResponse.class)
                .returnResult().getResponseBody().getRewards();

        assertThat(rewards.size()).isGreaterThan(0);
        assertThat(rewards).allMatch(reward -> reward.getBillingProfile().getId().equals(billingProfile.id()));
    }

    @Test
    @Order(4)
    void should_get_all_rewards_between_dates() {

        // When
        client.get()
                .uri(getApiURI(REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "COMPLETE",
                        "fromRequestedAt", "2023-02-08",
                        "toRequestedAt", "2023-02-10"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.status == 'COMPLETE')]").isArray()
                .jsonPath("$.rewards[?(@.status != 'COMPLETE')]").doesNotExist()
                .jsonPath("$.rewards[?(@.requestedAt >= '2023-02-08' && @.requestedAt < '2023-02-11')]").isArray()
                .jsonPath("$.rewards[?(@.requestedAt < '2023-02-08' || @.requestedAt >= '2023-02-11')]").doesNotExist();
    }

    @Test
    @Order(5)
    void should_get_all_rewards_with_recipient_id() {
        // When
        final var rewards = client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5", "recipients", "595505")))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RewardPageResponse.class)
                .returnResult().getResponseBody().getRewards();

        assertThat(rewards.size()).isGreaterThan(0);
        assertThat(rewards).allMatch(reward -> reward.getRecipient().getLogin().equals("ofux"));
    }

    @Test
    @Order(5)
    void should_get_all_rewards_with_project_id() {
        // When
        final var rewards = client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5", "projects", "7d04163c-4187-4313-8066-61504d34fc56")))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RewardPageResponse.class)
                .returnResult().getResponseBody().getRewards();

        assertThat(rewards.size()).isGreaterThan(0);
        assertThat(rewards).allMatch(reward -> reward.getProject().getName().equals("Bretzel"));
    }

    @Test
    @Order(10)
    void should_export_all_rewards_between_requested_dates() {

        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromRequestedAt", "2023-09-18", "toRequestedAt", "2023-09-21"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        final var assertableCsv = csv.replaceAll("202[456789]-\\d{2}-\\d{2}T\\d{2}:\\d{2}:[^,]+", "#PROCESSED_AT#");
        assertThat(assertableCsv).isEqualToIgnoringWhitespace("""
                Project,Invoice Creator Github,Invoice Creator email,Invoice Creator name,Recipient name,Amount,Currency,Contributions,Status,Requested at,Processed at,Transaction Hash,Payout information,Pretty ID,Sponsors,Recipient email,Verification status,Account type,Invoice number,Invoice id,Budget,Conversion rate,Dollar Amount
                QA new contributions,PierreOucif,pierre.oucif@gadz.org,Pierre Oucif,Pierre Qui roule n'amasse pas mousses,1000,USDC,"[https://github.com/onlydustxyz/marketplace-frontend/pull/1129, https://github.com/onlydustxyz/marketplace-frontend/pull/1138, https://github.com/onlydustxyz/marketplace-frontend/pull/1139, https://github.com/onlydustxyz/marketplace-frontend/pull/1157, https://github.com/onlydustxyz/marketplace-frontend/pull/1170, https://github.com/onlydustxyz/marketplace-frontend/pull/1171, https://github.com/onlydustxyz/marketplace-frontend/pull/1178, https://github.com/onlydustxyz/marketplace-frontend/pull/1179, https://github.com/onlydustxyz/marketplace-frontend/pull/1180, https://github.com/onlydustxyz/marketplace-frontend/pull/1183, https://github.com/onlydustxyz/marketplace-frontend/pull/1185, https://github.com/onlydustxyz/marketplace-frontend/pull/1186, https://github.com/onlydustxyz/marketplace-frontend/pull/1187, https://github.com/onlydustxyz/marketplace-frontend/pull/1188, https://github.com/onlydustxyz/marketplace-frontend/pull/1194, https://github.com/onlydustxyz/marketplace-frontend/pull/1195, https://github.com/onlydustxyz/marketplace-frontend/pull/1198, https://github.com/onlydustxyz/marketplace-frontend/pull/1200, https://github.com/onlydustxyz/marketplace-frontend/pull/1203, https://github.com/onlydustxyz/marketplace-frontend/pull/1206, https://github.com/onlydustxyz/marketplace-frontend/pull/1209, https://github.com/onlydustxyz/marketplace-frontend/pull/1212, https://github.com/onlydustxyz/marketplace-frontend/pull/1220, https://github.com/onlydustxyz/marketplace-frontend/pull/1225, https://github.com/onlydustxyz/marketplace-frontend/pull/1232]",COMPLETE,2023-09-19T07:40:26.971Z,#PROCESSED_AT#,[0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156],[fc92397c-3431-4a84-8054-845376b630a0.eth],"#40FDA",[No Sponsor],VERIFIED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,QA new contributions - USDC,1.01,1010.00
                QA new contributions,PierreOucif,pierre.oucif@gadz.org,Pierre Oucif,Pierre Qui roule n'amasse pas mousses,1000,USDC,"[https://github.com/onlydustxyz/marketplace-frontend/pull/1129, https://github.com/onlydustxyz/marketplace-frontend/pull/1138, https://github.com/onlydustxyz/marketplace-frontend/pull/1139, https://github.com/onlydustxyz/marketplace-frontend/pull/1157, https://github.com/onlydustxyz/marketplace-frontend/pull/1170, https://github.com/onlydustxyz/marketplace-frontend/pull/1171, https://github.com/onlydustxyz/marketplace-frontend/pull/1178, https://github.com/onlydustxyz/marketplace-frontend/pull/1179, https://github.com/onlydustxyz/marketplace-frontend/pull/1180, https://github.com/onlydustxyz/marketplace-frontend/pull/1183, https://github.com/onlydustxyz/marketplace-frontend/pull/1185, https://github.com/onlydustxyz/marketplace-frontend/pull/1186, https://github.com/onlydustxyz/marketplace-frontend/pull/1187, https://github.com/onlydustxyz/marketplace-frontend/pull/1188, https://github.com/onlydustxyz/marketplace-frontend/pull/1194, https://github.com/onlydustxyz/marketplace-frontend/pull/1195, https://github.com/onlydustxyz/marketplace-frontend/pull/1198, https://github.com/onlydustxyz/marketplace-frontend/pull/1200, https://github.com/onlydustxyz/marketplace-frontend/pull/1203, https://github.com/onlydustxyz/marketplace-frontend/pull/1206, https://github.com/onlydustxyz/marketplace-frontend/pull/1209, https://github.com/onlydustxyz/marketplace-frontend/pull/1212, https://github.com/onlydustxyz/marketplace-frontend/pull/1220, https://github.com/onlydustxyz/marketplace-frontend/pull/1225, https://github.com/onlydustxyz/marketplace-frontend/pull/1232]",COMPLETE,2023-09-19T07:39:54.456Z,#PROCESSED_AT#,[0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156],[fc92397c-3431-4a84-8054-845376b630a0.eth],"#8FE07",[No Sponsor],VERIFIED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,QA new contributions - USDC,1.01,1010.00
                QA new contributions,PierreOucif,pierre.oucif@gadz.org,Pierre Oucif,Pierre Qui roule n'amasse pas mousses,1000,USDC,"[https://github.com/onlydustxyz/marketplace-frontend/pull/1129, https://github.com/onlydustxyz/marketplace-frontend/pull/1138, https://github.com/onlydustxyz/marketplace-frontend/pull/1139, https://github.com/onlydustxyz/marketplace-frontend/pull/1157, https://github.com/onlydustxyz/marketplace-frontend/pull/1170, https://github.com/onlydustxyz/marketplace-frontend/pull/1171, https://github.com/onlydustxyz/marketplace-frontend/pull/1178, https://github.com/onlydustxyz/marketplace-frontend/pull/1179, https://github.com/onlydustxyz/marketplace-frontend/pull/1180, https://github.com/onlydustxyz/marketplace-frontend/pull/1183, https://github.com/onlydustxyz/marketplace-frontend/pull/1185, https://github.com/onlydustxyz/marketplace-frontend/pull/1186, https://github.com/onlydustxyz/marketplace-frontend/pull/1187, https://github.com/onlydustxyz/marketplace-frontend/pull/1188, https://github.com/onlydustxyz/marketplace-frontend/pull/1194, https://github.com/onlydustxyz/marketplace-frontend/pull/1195, https://github.com/onlydustxyz/marketplace-frontend/pull/1198, https://github.com/onlydustxyz/marketplace-frontend/pull/1200, https://github.com/onlydustxyz/marketplace-frontend/pull/1203, https://github.com/onlydustxyz/marketplace-frontend/pull/1206, https://github.com/onlydustxyz/marketplace-frontend/pull/1209, https://github.com/onlydustxyz/marketplace-frontend/pull/1212, https://github.com/onlydustxyz/marketplace-frontend/pull/1220, https://github.com/onlydustxyz/marketplace-frontend/pull/1225, https://github.com/onlydustxyz/marketplace-frontend/pull/1232]",COMPLETE,2023-09-19T07:39:23.730Z,#PROCESSED_AT#,[0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156],[fc92397c-3431-4a84-8054-845376b630a0.eth],"#5B96C",[No Sponsor],VERIFIED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,QA new contributions - USDC,1.01,1010.00
                QA new contributions,PierreOucif,pierre.oucif@gadz.org,Pierre Oucif,Pierre Qui roule n'amasse pas mousses,1000,USDC,"[https://github.com/onlydustxyz/marketplace-frontend/pull/1129, https://github.com/onlydustxyz/marketplace-frontend/pull/1138, https://github.com/onlydustxyz/marketplace-frontend/pull/1139, https://github.com/onlydustxyz/marketplace-frontend/pull/1157, https://github.com/onlydustxyz/marketplace-frontend/pull/1170, https://github.com/onlydustxyz/marketplace-frontend/pull/1171, https://github.com/onlydustxyz/marketplace-frontend/pull/1178, https://github.com/onlydustxyz/marketplace-frontend/pull/1179, https://github.com/onlydustxyz/marketplace-frontend/pull/1180, https://github.com/onlydustxyz/marketplace-frontend/pull/1183, https://github.com/onlydustxyz/marketplace-frontend/pull/1185, https://github.com/onlydustxyz/marketplace-frontend/pull/1186, https://github.com/onlydustxyz/marketplace-frontend/pull/1187, https://github.com/onlydustxyz/marketplace-frontend/pull/1188, https://github.com/onlydustxyz/marketplace-frontend/pull/1194, https://github.com/onlydustxyz/marketplace-frontend/pull/1195, https://github.com/onlydustxyz/marketplace-frontend/pull/1198, https://github.com/onlydustxyz/marketplace-frontend/pull/1200, https://github.com/onlydustxyz/marketplace-frontend/pull/1203, https://github.com/onlydustxyz/marketplace-frontend/pull/1206, https://github.com/onlydustxyz/marketplace-frontend/pull/1209, https://github.com/onlydustxyz/marketplace-frontend/pull/1212, https://github.com/onlydustxyz/marketplace-frontend/pull/1220, https://github.com/onlydustxyz/marketplace-frontend/pull/1225, https://github.com/onlydustxyz/marketplace-frontend/pull/1232]",COMPLETE,2023-09-19T07:38:52.590Z,#PROCESSED_AT#,[0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156],[fc92397c-3431-4a84-8054-845376b630a0.eth],"#85F83",[No Sponsor],VERIFIED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,QA new contributions - USDC,1.01,1010.00
                QA new contributions,PierreOucif,pierre.oucif@gadz.org,Pierre Oucif,Pierre Qui roule n'amasse pas mousses,1000,USDC,"[https://github.com/onlydustxyz/marketplace-frontend/pull/1129, https://github.com/onlydustxyz/marketplace-frontend/pull/1138, https://github.com/onlydustxyz/marketplace-frontend/pull/1139, https://github.com/onlydustxyz/marketplace-frontend/pull/1157, https://github.com/onlydustxyz/marketplace-frontend/pull/1170, https://github.com/onlydustxyz/marketplace-frontend/pull/1171, https://github.com/onlydustxyz/marketplace-frontend/pull/1178, https://github.com/onlydustxyz/marketplace-frontend/pull/1179, https://github.com/onlydustxyz/marketplace-frontend/pull/1180, https://github.com/onlydustxyz/marketplace-frontend/pull/1183, https://github.com/onlydustxyz/marketplace-frontend/pull/1185, https://github.com/onlydustxyz/marketplace-frontend/pull/1186, https://github.com/onlydustxyz/marketplace-frontend/pull/1187, https://github.com/onlydustxyz/marketplace-frontend/pull/1188, https://github.com/onlydustxyz/marketplace-frontend/pull/1194, https://github.com/onlydustxyz/marketplace-frontend/pull/1195, https://github.com/onlydustxyz/marketplace-frontend/pull/1198, https://github.com/onlydustxyz/marketplace-frontend/pull/1200, https://github.com/onlydustxyz/marketplace-frontend/pull/1203, https://github.com/onlydustxyz/marketplace-frontend/pull/1206, https://github.com/onlydustxyz/marketplace-frontend/pull/1209, https://github.com/onlydustxyz/marketplace-frontend/pull/1212, https://github.com/onlydustxyz/marketplace-frontend/pull/1220, https://github.com/onlydustxyz/marketplace-frontend/pull/1225, https://github.com/onlydustxyz/marketplace-frontend/pull/1232]",COMPLETE,2023-09-19T07:38:22.018Z,#PROCESSED_AT#,[0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156],[fc92397c-3431-4a84-8054-845376b630a0.eth],"#2AC80",[No Sponsor],VERIFIED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,QA new contributions - USDC,1.01,1010.00
                """.formatted(pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0)));
    }

    @Test
    @Order(11)
    void should_export_all_rewards_for_a_given_billing_profile() {
        // Given
        olivier = UserId.of(userAuthHelper.authenticateOlivier().user().getId());
        final var billingProfile = billingProfileReadRepository.findByUserId(olivier.value()).stream()
                .filter(b -> b.type() == COMPANY)
                .findFirst().orElseThrow();

        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of(
                                "billingProfiles", billingProfile.id().toString(),
                                "fromRequestedAt", "2023-03-20",
                                "toRequestedAt", "2023-03-21"
                        ))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        final var rows = Arrays.stream(csv.split("\n")).skip(1).toList();
        assertThat(rows).allMatch(row -> row.contains("Olivier Inc."));
    }

    @Test
    @Order(12)
    void should_export_all_rewards_between_processed_dates() {
        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromProcessedAt", "2023-09-01", "toProcessedAt", "2023-10-01"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertThat(csv).isEqualToIgnoringWhitespace("""
                Project,Invoice Creator Github,Invoice Creator email,Invoice Creator name,Recipient name,Amount,Currency,Contributions,Status,Requested at,Processed at,Transaction Hash,Payout information,Pretty ID,Sponsors,Recipient email,Verification status,Account type,Invoice number,Invoice id,Budget,Conversion rate,Dollar Amount
                Bretzel,,,,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-26T15:57:29.834Z,2023-09-26T21:08:01.957Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#736E0",[Coca Cola],,,,,Bretzel - USDC,1.0100000000000000,1010.0000
                Bretzel,,,,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-26T08:43:36.823Z,2023-09-26T21:08:01.735Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#1C56D",[Coca Cola],,,,,Bretzel - USDC,1.0100000000000000,1010.0000
                Bretzel,,,,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-25T13:12:26.971Z,2023-09-26T21:08:01.601Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#4CCF3",[Coca Cola],,,,,Bretzel - USDC,1.0100000000000000,1010.0000
                Bretzel,,,,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-25T13:01:35.433Z,2023-09-26T21:25:50.605Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#CF65A",[Coca Cola],,,,,Bretzel - USDC,1.0100000000000000,1010.0000
                Bretzel,,,,gregcha,1000,USDC,[https://github.com/gregcha/bretzel-app/issues/1],COMPLETE,2023-08-10T14:25:38.310Z,2023-09-26T21:25:49.941Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#1CCB3",[Coca Cola],,,,,Bretzel - USDC,1.0100000000000000,1010.00
                Bretzel,,,,gregcha,1000,USDC,[https://github.com/gregcha/bretzel-site/issues/1],COMPLETE,2023-07-26T10:06:57.034Z,2023-09-26T21:25:48.826Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#B69D6",[Coca Cola],,,,,Bretzel - USDC,1.0100000000000000,1010.00
                Taco Tuesday,,,,gregcha,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-07-23T08:34:56.803Z,2023-09-26T21:28:32.053Z,[OK cool],[FR7640618802650004034616528],"#0D951",[Red Bull],,,,,Taco Tuesday - USD,1.00000000000000000000,1000
                Mooooooonlight,,,,oscarwroche,1000,USDC,[https://github.com/onlydustxyz/marketplace-frontend/pull/743],COMPLETE,2023-03-01T12:48:51.425Z,2023-09-26T20:22:12.865Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#AB855",[Theodo],,,,,Mooooooonlight - USDC,1.0100000000000000,1010.00
                Marketplace 2,,,,ofux,438,USD,[https://github.com/onlydustxyz/marketplace-frontend/pull/642],COMPLETE,2023-02-02T15:20:35.665Z,2023-09-26T20:24:00.439Z,[Coucou les filles],[GB33BUKB20201555555555],"#C5AE2",[No Sponsor],,,,,Marketplace 2 - USD,1.00000000000000000000,438
                """);
    }

    @Test
    @Order(13)
    void should_export_nothing_when_there_is_no_reward() {
        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromProcessedAt", "2099-01-01", "toProcessedAt", "2099-01-02"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertThat(csv).isEqualToIgnoringWhitespace("""
                Project,Invoice Creator Github,Invoice Creator email,Invoice Creator name,Recipient name,Amount,Currency,Contributions,Status,Requested at,Processed at,Transaction Hash,Payout information,Pretty ID,Sponsors,Recipient email,Verification status,Account type,Invoice number,Invoice id,Budget,Conversion rate,Dollar Amount
                """);
    }

    @Test
    @Order(14)
    void should_export_all_rewards_without_status_filter() {
        // When
        client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("fromProcessedAt", "2023-09-01", "toProcessedAt", "2023-10-01"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @Order(20)
    void should_get_earnings_of_the_whole_platform() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS)
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 1193273.78,
                          "amountsPerCurrency": [
                            {
                              "amount": 29313.00,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 29313.00,
                              "rewardCount": 21
                            },
                            {
                              "amount": 271778.00,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 1163960.78,
                              "rewardCount": 223
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(21)
    void should_get_no_earnings_with_some_specific_filters() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "fromRequestedAt", "2010-02-08",
                        "toRequestedAt", "2010-02-10"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 0,
                          "amountsPerCurrency": []
                        }
                        """);
    }

    @Test
    @Order(22)
    void should_get_earnings_with_requested_at_date_range() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "fromRequestedAt", "2023-02-08",
                        "toRequestedAt", "2024-02-10"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 151144.52,
                          "amountsPerCurrency": [
                            {
                              "amount": 22125.00,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 22125.00,
                              "rewardCount": 19
                            },
                            {
                              "amount": 128752.00,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 129019.52,
                              "rewardCount": 105
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(23)
    void should_get_earnings_with_statuses() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "statuses", "COMPLETE"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 1084927.88,
                          "amountsPerCurrency": [
                            {
                              "amount": 22563,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 22563,
                              "rewardCount": 15
                            },
                            {
                              "amount": 171188.00,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 1062364.88,
                              "rewardCount": 133
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "statuses", "COMPLETE,PROCESSING"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 1094737.88,
                          "amountsPerCurrency": [
                            {
                              "amount": 26313,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 26313,
                              "rewardCount": 18
                            },
                            {
                              "amount": 177188.00,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 1068424.88,
                              "rewardCount": 139
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(24)
    void should_get_earnings_with_billing_profiles() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "billingProfiles", "50d8ae0d-1981-435b-90c5-09fc32b7d7d6,9cae91ac-e70f-426f-af0d-e35c1d3578ed"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 985610.00,
                          "amountsPerCurrency": [
                            {
                              "amount": 18375.00,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 18375.00,
                              "rewardCount": 16
                            },
                            {
                              "amount": 77000.00,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 967235.00,
                              "rewardCount": 57
                            }
                          ]
                        }
                        """);
    }


    @Test
    @Order(25)
    void should_get_earnings_with_recipient() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "recipients", "595505"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 20855.02,
                          "amountsPerCurrency": [
                            {
                              "amount": 4188,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 4188,
                              "rewardCount": 4
                            },
                            {
                              "amount": 16502,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 16667.02,
                              "rewardCount": 11
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(25)
    void should_get_earnings_with_processed_at_date_range() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                        "fromProcessedAt", "2023-02-08",
                        "toProcessedAt", "2024-02-10"))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 998953.65,
                          "amountsPerCurrency": [
                            {
                              "amount": 22563,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 22563,
                              "rewardCount": 15
                            },
                            {
                              "amount": 86065.00,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 976390.65,
                              "rewardCount": 66
                            }
                          ]
                        }
                        """);
    }


    @Test
    @Order(26)
    void should_get_earnings_all_combined() {

        // When
        client.get()
                .uri(getApiURI(EARNINGS, Map.of(
                                "recipients", "43467246,595505",
                                "billingProfiles", "50d8ae0d-1981-435b-90c5-09fc32b7d7d6,9cae91ac-e70f-426f-af0d-e35c1d3578ed",
                                "statuses", "COMPLETE",
                                "fromRequestedAt", "2023-02-08",
                                "toRequestedAt", "2024-02-10",
                                "fromProcessedAt", "2023-02-08",
                                "toProcessedAt", "2024-02-10",
                                "projects", "7d04163c-4187-4313-8066-61504d34fc56,1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e,57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8"
                        ))
                )
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalUsdAmount": 4260.00,
                          "amountsPerCurrency": [
                            {
                              "amount": 3250,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "dollarsEquivalent": 3250,
                              "rewardCount": 1
                            },
                            {
                              "amount": 1000,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 1010.00,
                              "rewardCount": 1
                            }
                          ]
                        }
                        """);
    }
}
