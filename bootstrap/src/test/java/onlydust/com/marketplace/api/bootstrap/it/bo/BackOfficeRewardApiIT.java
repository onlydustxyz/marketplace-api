package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.backoffice.api.contract.model.SearchRewardItemResponse;
import onlydust.com.backoffice.api.contract.model.SearchRewardsResponse;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiHttpClient;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeRewardApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    Config webhookHttpClientProperties;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    private final Faker faker = new Faker();

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
        this.anthony = UserId.of(userAuthHelper.authenticateAnthony().user().getId());
        this.olivier = UserId.of(userAuthHelper.authenticateOlivier().user().getId());
        this.pierre = UserId.of(userAuthHelper.authenticatePierre().user().getId());

        olivierBillingProfile = billingProfileService.createCompanyBillingProfile(this.olivier, "Apple Inc.", null);
        billingProfileService.updatePayoutInfo(olivierBillingProfile.id(), this.olivier,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.olivier + ".eth"))).build());

        anthonyBillingProfile = billingProfileService.createSelfEmployedBillingProfile(this.anthony, "Olivier SASU", null);
        billingProfileService.updatePayoutInfo(anthonyBillingProfile.id(), this.anthony,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.anthony + ".eth"))).build());

        pierreBillingProfile = billingProfileService.createIndividualBillingProfile(this.pierre, "Olivier", null);
        billingProfileService.updatePayoutInfo(pierreBillingProfile.id(), this.pierre,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.pierre + ".eth"))).build());

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
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));
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
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));
        kycRepository.findByBillingProfileId(pierreBillingProfile.id().value())
                .ifPresent(kyc -> kycRepository.save(kyc.toBuilder()
                        .country("FRA")
                        .address("3 Infinite Loop, Cupertino, CA 95014, United States")
                        .firstName("Pierre")
                        .lastName("Qui roule n'amasse pas mousses")
                        .birthdate(faker.date().birthday())
                        .usCitizen(false)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));

        // Given
        newOlivierInvoiceToReview(List.of(
                RewardId.of("061e2c7e-bda4-49a8-9914-2e76926f70c2")));
        newOlivierInvoiceToReview(List.of(
                RewardId.of("ee28315c-7a84-4052-9308-c2236eeafda1"),
                RewardId.of("d067b24d-115a-45e9-92de-94dd1d01b184")));
        newOlivierInvoiceToReview(List.of(
                RewardId.of("d506a05d-3739-452f-928d-45ea81d33079"),
                RewardId.of("5083ac1f-4325-4d47-9760-cbc9ab82f25c"),
                RewardId.of("e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236")));

        newAnthonyInvoiceToReview(List.of(
                RewardId.of("5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c"),
                RewardId.of("fab7aaf4-9b0c-4e52-bc9b-72ce08131617"),
                RewardId.of("736e0554-f30e-4315-9731-7611fa089dcf")));

        newPierreInvoiceToReview(List.of(
                RewardId.of("bdb59436-1b93-4c3c-a6e2-b8b09411280c"),
                RewardId.of("e23ad82b-27c5-4840-9481-da31aef6ba1b"),
                RewardId.of("72f257fa-1b20-433d-9cdd-88d5182b7369"),
                RewardId.of("91c7e960-37ba-4334-ba91-f1b02f1927ab"),
                RewardId.of("e6152967-9bd4-40e6-bad5-c9c4a9578d0f")));
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

    @Test
    @Order(1)
    void should_search_payable_rewards_to_pay_given_a_list_of_invoice_id() throws IOException {
        // Given
        setUp();

        // When
        final SearchRewardsResponse searchRewardsResponse = client.post()
                .uri(getApiURI(POST_REWARDS_SEARCH))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                            "invoiceIds": ["%s","%s","%s"]
                            }
                        """.formatted(
                        olivierInvoiceIds.get(0).value(),
                        olivierInvoiceIds.get(1).value(),
                        olivierInvoiceIds.get(2).value()
                ))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(SearchRewardsResponse.class)
                .getResponseBody().blockFirst();
        final List<UUID> expectedRewardIds = List.of(
                UUID.fromString("061e2c7e-bda4-49a8-9914-2e76926f70c2"),
                UUID.fromString("ee28315c-7a84-4052-9308-c2236eeafda1"),
                UUID.fromString("d067b24d-115a-45e9-92de-94dd1d01b184"),
                UUID.fromString("d506a05d-3739-452f-928d-45ea81d33079"),
                UUID.fromString("5083ac1f-4325-4d47-9760-cbc9ab82f25c"),
                UUID.fromString("e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236")
        );
        for (SearchRewardItemResponse reward : searchRewardsResponse.getRewards()) {
            assertThat(expectedRewardIds).contains(reward.getId());
            assertThat(List.of(Currency.Code.USDC_STR, Currency.Code.LORDS_STR, Currency.Code.STRK_STR)).contains(reward.getMoney().getCurrencyCode());
        }
    }

    @Test
    @Order(2)
    void should_get_all_rewards() {

        // When
        client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 29,
                          "totalItemNumber": 143,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "rewards": [
                            {
                              "id": "5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c",
                              "billingProfile": {
                                "name": null,
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-ANTHO-SASU-001",
                                "status": "TO_REVIEW"
                              },
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:09:31.842962Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.0,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                              "billingProfile": {
                                "name": null,
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-ANTHO-SASU-001",
                                "status": "TO_REVIEW"
                              },
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:06:42.730697Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.0,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "64fb2732-5632-4b09-a8b1-217485648129",
                              "billingProfile": null,
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:00:31.105159Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.0,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "736e0554-f30e-4315-9731-7611fa089dcf",
                              "billingProfile": {
                                "name": null,
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-ANTHO-SASU-001",
                                "status": "TO_REVIEW"
                              },
                              "status": "COMPLETE",
                              "requestedAt": "2023-09-26T15:57:29.834949Z",
                              "processedAt": "2023-09-26T21:08:01.957Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1010.0000,
                                "conversionRate": 1.01,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db"
                              ],
                              "paidTo": [
                                "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                              ]
                            },
                            {
                              "id": "1c56d096-5284-4ae3-af3c-dd2b3211fb73",
                              "billingProfile": null,
                              "invoice": null,
                              "status": "COMPLETE",
                              "requestedAt": "2023-09-26T08:43:36.823851Z",
                              "processedAt": "2023-09-26T21:08:01.735916Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1010.0000,
                                "conversionRate": 1.01,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db"
                              ],
                              "paidTo": [
                                "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(3)
    void should_get_all_rewards_with_status() {

        // When
        client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5", "statuses", "PENDING_VERIFICATION")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 9,
                          "totalItemNumber": 45,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "rewards": [
                            {
                              "id": "5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c",
                              "billingProfile": {
                                "name": null,
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-ANTHO-SASU-001",
                                "status": "TO_REVIEW"
                              },
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:09:31.842962Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.0,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                              "billingProfile": {
                                "name": null,
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-ANTHO-SASU-001",
                                "status": "TO_REVIEW"
                              },
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:06:42.730697Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.0,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "64fb2732-5632-4b09-a8b1-217485648129",
                              "billingProfile": null,
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:00:31.105159Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "sponsors": [
                                {
                                  "name": "Coca Cola",
                                  "avatarUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                },
                                {
                                  "name": "OGC Nissa Ineos",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "money": {
                                "amount": 1000.00,
                                "dollarsEquivalent": 1000.00,
                                "conversionRate": 1.0,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a",
                              "billingProfile": null,
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-09-20T08:46:52.77875Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1221"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "PierreOucif",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "project": {
                                "name": "QA new contributions",
                                "logoUrl": null
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": 1.01,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            },
                            {
                              "id": "f0c1b882-76f2-47d0-9331-151ce1f99281",
                              "billingProfile": null,
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-09-20T08:45:02.552217Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1240"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "AnthonyBuisset",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                              },
                              "project": {
                                "name": "kaaper",
                                "logoUrl": null
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": null,
                                "conversionRate": null,
                                "currencyCode": "STRK",
                                "currencyName": "StarkNet Token",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [],
                              "paidTo": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(4)
    void should_get_all_rewards_between_dates() {

        // When
        client.get()
                .uri(getApiURI(REWARDS, Map.of("pageIndex", "0", "pageSize", "5", "statuses", "COMPLETE",
                        "fromRequestedAt", "2023-02-08", "toRequestedAt", "2023-02-10"))
                )
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 5,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "rewards": [
                            {
                              "id": "bdb59436-1b93-4c3c-a6e2-b8b09411280c",
                              "billingProfile": {
                                "name": null,
                                "type": "INDIVIDUAL",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Qui roule n'amasse pas mousses",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001",
                                "status": "APPROVED"
                              },
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:48.146947Z",
                              "processedAt": "2023-02-09T07:35:03.828Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": 1.0100000000000000,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x0"
                              ],
                              "paidTo": [
                                "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                              ]
                            },
                            {
                              "id": "e23ad82b-27c5-4840-9481-da31aef6ba1b",
                              "billingProfile": {
                                "name": null,
                                "type": "INDIVIDUAL",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Qui roule n'amasse pas mousses",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001",
                                "status": "APPROVED"
                              },
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:40.924453Z",
                              "processedAt": "2023-02-09T07:35:03.417Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 2500,
                                "dollarsEquivalent": 2525.00,
                                "conversionRate": 1.0100000000000000,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x0"
                              ],
                              "paidTo": [
                                "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                              ]
                            },
                            {
                              "id": "72f257fa-1b20-433d-9cdd-88d5182b7369",
                              "billingProfile": {
                                "name": null,
                                "type": "INDIVIDUAL",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Qui roule n'amasse pas mousses",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001",
                                "status": "APPROVED"
                              },
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:31.946777Z",
                              "processedAt": "2023-02-09T07:35:02.985Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 750,
                                "dollarsEquivalent": 757.50,
                                "conversionRate": 1.0100000000000000,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x0"
                              ],
                              "paidTo": [
                                "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                              ]
                            },
                            {
                              "id": "91c7e960-37ba-4334-ba91-f1b02f1927ab",
                              "billingProfile": {
                                "name": null,
                                "type": "INDIVIDUAL",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Qui roule n'amasse pas mousses",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001",
                                "status": "APPROVED"
                              },
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:20.274391Z",
                              "processedAt": "2023-02-09T07:35:02.582Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": 1.0100000000000000,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x0"
                              ],
                              "paidTo": [
                                "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                              ]
                            },
                            {
                              "id": "e6152967-9bd4-40e6-bad5-c9c4a9578d0f",
                              "billingProfile": {
                                "name": null,
                                "type": "INDIVIDUAL",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Qui roule n'amasse pas mousses",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001",
                                "status": "APPROVED"
                              },
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-08T09:14:56.053584Z",
                              "processedAt": "2023-02-27T11:56:28.044Z",
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/663"
                              ],
                              "paidNotificationDate": null,
                              "recipient": {
                                "login": "oscarwroche",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4"
                              },
                              "project": {
                                "name": "oscar's awesome project",
                                "logoUrl": null
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 500,
                                "dollarsEquivalent": 505.00,
                                "conversionRate": 1.0100000000000000,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionReferences": [
                                "0x0"
                              ],
                              "paidTo": [
                                "0xd8da6bf26964af9d7eed9e03e53415d37aa96045"
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(5)
    void should_export_all_rewards_between_requested_dates() {

        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromRequestedAt", "2023-02-08", "toRequestedAt", "2023-02-10"))
                )
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertThat(csv).isEqualToIgnoringWhitespace("""
                Project,Recipient,Recipient Github,Amount,Currency,Contributions,Status,Requested at,Processed at,Transaction Hash,Payout information,Pretty ID,Sponsors,Recipient email,Verification status,Account type,Invoice number,Invoice id,Budget,Conversion rate,Dollar Amount
                Starklings,"[PierreOucif,pierre.oucif@gadz.org,Pierre Qui roule n'amasse pas mousses]",1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:48.146947Z,2023-02-09T07:35:03.828Z,[0x0],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#BDB59",[No Sponsor],NOT_STARTED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,Starklings - USDC,1.01,1010.00
                Starklings,"[PierreOucif,pierre.oucif@gadz.org,Pierre Qui roule n'amasse pas mousses]",2500,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:40.924453Z,2023-02-09T07:35:03.417Z,[0x0],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#E23AD",[No Sponsor],NOT_STARTED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,Starklings - USDC,1.01,2525.00
                Starklings,"[PierreOucif,pierre.oucif@gadz.org,Pierre Qui roule n'amasse pas mousses]",750,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:31.946777Z,2023-02-09T07:35:02.985Z,[0x0],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#72F25",[No Sponsor],NOT_STARTED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,Starklings - USDC,1.01,757.50
                Starklings,"[PierreOucif,pierre.oucif@gadz.org,Pierre Qui roule n'amasse pas mousses]",1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:20.274391Z,2023-02-09T07:35:02.582Z,[0x0],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#91C7E",[No Sponsor],NOT_STARTED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,Starklings - USDC,1.01,1010.00
                oscar's awesome project,"[PierreOucif,pierre.oucif@gadz.org,Pierre Qui roule n'amasse pas mousses]",500,USDC,[https://github.com/onlydustxyz/marketplace-frontend/pull/663],COMPLETE,2023-02-08T09:14:56.053584Z,2023-02-27T11:56:28.044Z,[0x0],[0xd8da6bf26964af9d7eed9e03e53415d37aa96045],"#E6152",[No Sponsor],NOT_STARTED,INDIVIDUAL,OD-QUI-ROULE-N-AMASSE-PAS-MOUSSES-PIERRE-001,%s,oscar's awesome project - USDC,1.01,505.00
                """.formatted(pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0),
                pierreInvoiceIds.get(0)));
    }

    @Test
    @Order(6)
    void should_export_all_rewards_between_processed_dates() {
        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromProcessedAt", "2023-09-01", "toProcessedAt", "2023-10-01"))
                )
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertThat(csv).isEqualToIgnoringWhitespace("""
                Project,Recipient,Recipient Github,Amount,Currency,Contributions,Status,Requested at,Processed at,Transaction Hash,Payout information,Pretty ID,Sponsors,Recipient email,Verification status,Account type,Invoice number,Invoice id,Budget,Conversion rate,Dollar Amount
                Bretzel,"[AnthonyBuisset,abuisset@gmail.com,Anthony BUISSET]",1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-26T15:57:29.834949Z,2023-09-26T21:08:01.957Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#736E0","[Coca Cola, OGC Nissa Ineos]",NOT_STARTED,SELF_EMPLOYED,OD-ANTHO-SASU-001,%s,Bretzel - USDC,1.0100,1010.0000
                Bretzel,,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-26T08:43:36.823851Z,2023-09-26T21:08:01.735916Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#1C56D","[Coca Cola, OGC Nissa Ineos]",,,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-25T13:12:26.971685Z,2023-09-26T21:08:01.601831Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#4CCF3","[Coca Cola, OGC Nissa Ineos]",,,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-25T13:01:35.433511Z,2023-09-26T21:25:50.605546Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#CF65A","[Coca Cola, OGC Nissa Ineos]",,,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,,1000,USDC,[https://github.com/gregcha/bretzel-app/issues/1],COMPLETE,2023-08-10T14:25:38.310796Z,2023-09-26T21:25:49.941482Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#1CCB3","[Coca Cola, OGC Nissa Ineos]",,,,,Bretzel - USDC,1.01,1010.00
                Bretzel,,1000,USDC,[https://github.com/gregcha/bretzel-site/issues/1],COMPLETE,2023-07-26T10:06:57.034426Z,2023-09-26T21:25:48.826952Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#B69D6","[Coca Cola, OGC Nissa Ineos]",,,,,Bretzel - USDC,1.01,1010.00
                Taco Tuesday,,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-07-23T08:34:56.803043Z,2023-09-26T21:28:32.053680Z,[OK cool],[FR7640618802650004034616528],"#0D951",[Red Bull],,,,,Taco Tuesday - USD,1,1000
                Mooooooonlight,,1000,USDC,[https://github.com/onlydustxyz/marketplace-frontend/pull/743],COMPLETE,2023-03-01T12:48:51.425766Z,2023-09-26T20:22:12.865097Z,[0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db],[0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea],"#AB855","[Starknet Foundation, Theodo]",,,,,Mooooooonlight - USDC,1.01,1010.00
                Marketplace 2,,438,USD,[https://github.com/onlydustxyz/marketplace-frontend/pull/642],COMPLETE,2023-02-02T15:20:35.665817Z,2023-09-26T20:24:00.439566Z,[Coucou les filles],[GB33BUKB20201555555555],"#C5AE2",[No Sponsor],,,,,Marketplace 2 - USD,1,438
                """.formatted(anthonyInvoiceIds.get(0)));
    }

    @Test
    @Order(7)
    void should_export_nothing_when_there_is_no_reward() {
        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromProcessedAt", "2099-01-01", "toProcessedAt", "2099-01-02"))
                )
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertThat(csv).isEqualToIgnoringWhitespace("""
                Project,Recipient,Recipient Github,Amount,Currency,Contributions,Status,Requested at,Processed at,Transaction Hash,Payout information,Pretty ID,Sponsors,Recipient email,Verification status,Account type,Invoice number,Invoice id,Budget,Conversion rate,Dollar Amount
                """);
    }

    @Test
    @Order(100)
    void should_post_batch_payments_given_list_of_invoice_ids() {
        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_BATCH_PAYMENTS))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                            "invoiceIds": ["%s","%s","%s"]
                            }
                        """.formatted(
                        olivierInvoiceIds.get(0),
                        olivierInvoiceIds.get(1),
                        olivierInvoiceIds.get(2)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""

                                                {
                          "batchPayments": [
                            {
                              "csv": "erc20,0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0,0x716E30e2981481bc56CCc315171A9E2923bD12B4,11.22, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,11.22, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, \\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000, ",
                              "blockchain": "ETHEREUM",
                              "rewardCount": 12,
                              "totalAmounts": [
                                {
                                  "amount": 11.22,
                                  "dollarsEquivalent": 22,
                                  "conversionRate": null,
                                  "currencyCode": "LORDS",
                                  "currencyName": "Lords",
                                  "currencyLogoUrl": null
                                },
                                {
                                  "amount": 10011.22,
                                  "dollarsEquivalent": 12982.00,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                }
                              ]
                            },
                            {
                              "csv": "erc20,0x04718f5a0fc34cc1af16a1cdee98ffb20c31f5cd61d6ab07201858f4287c938d,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7,11.222, \\nerc20,0x04718f5a0fc34cc1af16a1cdee98ffb20c31f5cd61d6ab07201858f4287c938d,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc8,11522, ",
                              "blockchain": "STARKNET",
                              "rewardCount": 2,
                              "totalAmounts": [
                                {
                                  "amount": 11533.222,
                                  "dollarsEquivalent": 544,
                                  "conversionRate": null,
                                  "currencyCode": "STRK",
                                  "currencyName": "StarkNet Token",
                                  "currencyLogoUrl": null
                                }
                              ]
                            }
                          ]
                        }""");
    }

    @Autowired
    BatchPaymentRepository batchPaymentRepository;
    @Autowired
    OdRustApiHttpClient.Properties odRustApiHttpClientProperties;
    @Autowired
    ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig;


    //TODO
//    @Test
//    @Order(103)
//    void should_pay_strk_batch_payment() {
//        // Given
//        final var toto = batchPaymentRepository.findAll();
//        final BatchPaymentEntity starknetBatchPaymentEntity = batchPaymentRepository.findAll().stream()
//                .filter(batchPaymentEntity -> batchPaymentEntity.getNetwork().equals(NetworkEnumEntity.starknet))
//                .findFirst()
//                .orElseThrow();
//        final String transactionHash = "0x" + faker.random().hex();
//
//        final PaymentRequestEntity r1 = paymentRequestRepository.findById(starknetBatchPaymentEntity.getRewardIds().get(0)).orElseThrow();
//        final PaymentRequestEntity r2 = paymentRequestRepository.findById(starknetBatchPaymentEntity.getRewardIds().get(1)).orElseThrow();
//
//
//        rustApiWireMockServer.stubFor(
//                WireMock.post("/api/payments/%s/receipts".formatted(r1.getId()))
//                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
//                        .withRequestBody(WireMock.equalToJson(
//                                """
//                                        {
//                                           "amount": %s,
//                                           "currency": "%s",
//                                           "recipientWallet": "%s",
//                                           "recipientIban" : null,
//                                           "transactionReference" : "%s"
//                                        }
//                                            """.formatted(r1.getAmount().toString(), r1.getCurrency().toDomain().name(), anthoStarknetAddress,
//                                            transactionHash)
//                        ))
//                        .willReturn(ResponseDefinitionBuilder.okForJson("""
//                                {
//                                    "receipt_id": "%s"
//                                }""".formatted(UUID.randomUUID()))));
//
//        rustApiWireMockServer.stubFor(
//                WireMock.post("/api/payments/%s/receipts".formatted(r2.getId()))
//                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
//                        .withRequestBody(WireMock.equalToJson(
//                                """
//                                        {
//                                           "amount": %s,
//                                           "currency": "%s",
//                                           "recipientWallet": "%s",
//                                           "recipientIban" : null,
//                                           "transactionReference" : "%s"
//                                        }
//                                        """.formatted(r2.getAmount().toString(), r2.getCurrency().toDomain().name(), olivierStarknetAddress,
//                                        transactionHash)
//                        ))
//                        .willReturn(ResponseDefinitionBuilder.okForJson("""
//                                {
//                                    "receipt_id": "%s"
//                                }""".formatted(UUID.randomUUID()))));
//
//
//        // When
//        client.put()
//                .uri(getApiURI(PUT_REWARDS_BATCH_PAYMENTS.formatted(starknetBatchPaymentEntity.getId())))
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Api-Key", apiKey())
//                .bodyValue("""
//                                          {
//                                            "transactionHash": "%s"
//                                          }
//                        """.formatted(transactionHash))
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful();
//
//        final BatchPayment batchPayment = batchPaymentRepository.findById(starknetBatchPaymentEntity.getId()).orElseThrow().toDomain();
//        assertEquals(BatchPayment.Status.PAID, batchPayment.status());
//        assertTrue(batchPayment.rewardIds().contains(RewardId.of(r1.getId())));
//        assertTrue(batchPayment.rewardIds().contains(RewardId.of(r2.getId())));
//    }


    @Test
    @Order(104)
    void should_get_page_of_payment_batch_and_get_payment_batch_by_id() {
        // Given
        final BatchPaymentEntity starknetBatchPaymentEntity = batchPaymentRepository.findAll().stream()
                .filter(batchPaymentEntity -> batchPaymentEntity.getNetwork().equals(NetworkEnumEntity.starknet))
                .findFirst()
                .orElseThrow();

        // When
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "20")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_BATCH_PAYMENTS_PAGE_JSON_RESPONSE);

        // When
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(starknetBatchPaymentEntity.getId())))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.blockchain").isEqualTo("STARKNET")
                .jsonPath("$.rewardCount").isEqualTo(2)
                .jsonPath("$.totalAmountUsd").isEqualTo(544)
                .jsonPath("$.totalAmounts[0].amount").isEqualTo(11533.222)
                .jsonPath("$.totalAmounts.length()").isEqualTo(1)
                .jsonPath("$.csv").isNotEmpty()
                .jsonPath("$.transactionHash").isNotEmpty()
                .jsonPath("$.rewards.length()").isEqualTo(2);
    }

//    @Autowired
//    PaymentRepository paymentRepository;
//    @Autowired
//    Config webhookHttpClientProperties;
//
//    @Test
//    @Order(110)
//    void should_notify_new_rewards_paid() {
//        // Given
//        final List<PaymentRequestEntity> rewardsToPay = paymentRequestRepository.findAllById(
//                batchPaymentRepository.findAll().stream()
//                        .flatMap(batchPaymentEntity -> batchPaymentEntity.getRewardIds().stream())
//                        .toList());
//        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "ETH", JacksonUtil.toJsonNode("{}"), rewardsToPay.get(0).getId(),
//                new Date()));
//        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "ETH", JacksonUtil.toJsonNode("{}"), rewardsToPay.get(1).getId(),
//                new Date()));
//
//        makeWebhookSendRewardsPaidMailWireMockServer.stubFor(
//                post("/?api-key=%s".formatted(webhookHttpClientProperties.getApiKey()))
//                        .willReturn(ok()));
//
//        // When
//        client.put()
//                .uri(getApiURI(PUT_REWARDS_NOTIFY_PAYMENTS))
//                .header("Api-Key", apiKey())
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful();
//
//        makeWebhookSendRewardsPaidMailWireMockServer.verify(1,
//                postRequestedFor(urlEqualTo("/?api-key=%s".formatted(webhookHttpClientProperties.getApiKey())))
//                        .withHeader("Content-Type", equalTo("application/json"))
//                        .withRequestBody(matchingJsonPath("$.recipientEmail", equalTo("abuisset@gmail.com")))
//                        .withRequestBody(matchingJsonPath("$.recipientName", equalTo("Anthony BUISSET"))));
//    }

    private static final String GET_BATCH_PAYMENTS_PAGE_JSON_RESPONSE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 1,
              "hasMore": false,
              "nextPageIndex": 0,
              "batchPayments": [
                {
                  "blockchain": "STARKNET",
                  "rewardCount": 2,
                  "totalAmountUsd": 544,
                  "totalAmounts": [
                    {
                      "amount": 11533.222,
                      "dollarsEquivalent": 544,
                      "conversionRate": null,
                      "currencyCode": "STRK",
                      "currencyName": "StarkNet Token",
                      "currencyLogoUrl": null
                    }
                  ]
                }
              ]
            }
            """;
}
