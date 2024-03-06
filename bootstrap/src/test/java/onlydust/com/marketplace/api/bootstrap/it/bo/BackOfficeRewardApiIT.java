package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.backoffice.api.contract.model.SearchRewardItemResponse;
import onlydust.com.backoffice.api.contract.model.SearchRewardsResponse;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiHttpClient;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldVerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.project.domain.model.OldAccountNumber;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.project.domain.service.UserService;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeRewardApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;
    @Autowired
    UserService userService;
    @Autowired
    UserPayoutInfoRepository userPayoutInfoRepository;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    InvoiceStoragePort invoiceStoragePort;
    static List<UUID> invoiceIds = new ArrayList<>();
    final StarknetAccountAddress olivierStarknetAddress = new StarknetAccountAddress("0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc8");
    final StarknetAccountAddress anthoStarknetAddress = new StarknetAccountAddress("0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7");

    @Test
    @Order(1)
    void should_search_payable_rewards_to_pay_given_a_list_of_invoice_id() {
        // Given
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();
        userService.getCompanyBillingProfile(olivier.user().getId());

        final UserAuthHelper.AuthenticatedUser anthony = userAuthHelper.authenticateAnthony();
        userService.getCompanyBillingProfile(anthony.user().getId());


        final var olivierBillingProfile = companyBillingProfileRepository.findByUserId(olivier.user().getId()).orElseThrow();
        olivierBillingProfile.setName("O My company");
        olivierBillingProfile.setCountry("FRA");
        olivierBillingProfile.setAddress("O My address");
        olivierBillingProfile.setRegistrationNumber("O123456");
        olivierBillingProfile.setSubjectToEuVAT(true);
        olivierBillingProfile.setVerificationStatus(OldVerificationStatusEntity.VERIFIED);
        companyBillingProfileRepository.save(olivierBillingProfile);

        final var anthonyBillingProfile = companyBillingProfileRepository.findByUserId(anthony.user().getId()).orElseThrow();
        anthonyBillingProfile.setName("A My company");
        anthonyBillingProfile.setCountry("FRA");
        anthonyBillingProfile.setAddress("A My address");
        anthonyBillingProfile.setRegistrationNumber("A 123456");
        anthonyBillingProfile.setSubjectToEuVAT(true);
        anthonyBillingProfile.setVerificationStatus(OldVerificationStatusEntity.VERIFIED);
        companyBillingProfileRepository.save(anthonyBillingProfile);

        userService.updatePayoutSettings(olivier.user().getId(), UserPayoutSettings.builder()
                .ethWallet(new Wallet(new EvmAccountAddress("0x716E30e2981481bc56CCc315171A9E2923bD12B4")))
                .aptosAddress(new AptosAccountAddress("0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5"))
                .optimismAddress(new EvmAccountAddress("0x716E30e2981481bc56CCc315171A9E2923bD12B4"))
                .starknetAddress(olivierStarknetAddress)
                .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                        .accountNumber(OldAccountNumber.of("FR24 1009 6000 4032 5458 9765 X13"))
                        .bic("BOUSFRPPXXX")
                        .build())
                .build());

        userService.updatePayoutSettings(anthony.user().getId(), UserPayoutSettings.builder()
                .ethWallet(new Wallet(new EvmAccountAddress("0x716E30e2981481bc56CCc315171A9E2923bD12B4")))
                .aptosAddress(new AptosAccountAddress("0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5"))
                .optimismAddress(new EvmAccountAddress("0x716E30e2981481bc56CCc315171A9E2923bD12B4"))
                .starknetAddress(anthoStarknetAddress)
                .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                        .accountNumber(OldAccountNumber.of("FR24 1009 6000 4032 5458 9765 X13"))
                        .bic("BOUSFRPPXXX")
                        .build())
                .build());

        final var requestedAt = ZonedDateTime.parse("2024-03-05T19:00:00.000Z");
        final List<ProjectEntity> projects = projectRepository.findAll();
        paymentRequestRepository.saveAll(List.of(
                new PaymentRequestEntity(UUID.fromString("5ae4a031-2676-4a96-8ff3-65a934f06fa9"), anthony.user().getId(), anthony.user().getGithubUserId(),
                        DateMapper.ofNullable(requestedAt.minusMinutes(1)), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(0).getId(), CurrencyEnumEntity.lords, BigDecimal.valueOf(22)),
                new PaymentRequestEntity(UUID.fromString("bb790ead-639e-41ff-a6c9-7e8c240cad14"), anthony.user().getId(), anthony.user().getGithubUserId(),
                        DateMapper.ofNullable(requestedAt.minusMinutes(2)), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(1).getId(), CurrencyEnumEntity.op, BigDecimal.valueOf(2212)),
                new PaymentRequestEntity(UUID.fromString("afb0d66f-6ccb-4c72-bfa1-22e8aaac12ec"), olivier.user().getId(), anthony.user().getGithubUserId(),
                        DateMapper.ofNullable(requestedAt.minusMinutes(3)), BigDecimal.valueOf(11.222),
                        null, 0, projects.get(2).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(322)),
                new PaymentRequestEntity(UUID.fromString("a2cb3b32-921a-48da-af29-a1e038c6c341"), anthony.user().getId(), olivier.user().getGithubUserId(),
                        DateMapper.ofNullable(requestedAt.minusMinutes(4)), BigDecimal.valueOf(11522),
                        null, 0, projects.get(3).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(222)),
                new PaymentRequestEntity(UUID.fromString("e3ab855b-f6c4-485f-8d34-aa6cfb99e2b3"), anthony.user().getId(), olivier.user().getGithubUserId(),
                        DateMapper.ofNullable(requestedAt.minusMinutes(5)), BigDecimal.valueOf(171.22),
                        null, 0, projects.get(3).getId(), CurrencyEnumEntity.usd, BigDecimal.valueOf(122)),
                new PaymentRequestEntity(UUID.fromString("56a647e2-9ec7-4383-a91a-d41ce899682c"), olivier.user().getId(), anthony.user().getGithubUserId(),
                        DateMapper.ofNullable(requestedAt.minusMinutes(6)), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(2).getId(), CurrencyEnumEntity.usdc, BigDecimal.valueOf(2882))
        ));

        final List<UserRewardView> olivierRewardsPendingInvoice = userService.getPendingInvoiceRewardsForRecipientId(olivier.user().getGithubUserId());
        final List<UserRewardView> anthoRewardsPendingInvoice = userService.getPendingInvoiceRewardsForRecipientId(anthony.user().getGithubUserId());

        final Invoice olivierInvoice1 = billingProfileService.previewInvoice(UserId.of(olivier.user().getId()),
                BillingProfile.Id.of(olivierBillingProfile.getId()),
                olivierRewardsPendingInvoice.subList(0, 3).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());

        final Invoice anthoInvoice1 = billingProfileService.previewInvoice(UserId.of(anthony.user().getId()),
                BillingProfile.Id.of(anthonyBillingProfile.getId()),
                anthoRewardsPendingInvoice.subList(0, 7).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());

        invoiceStoragePort.update(olivierInvoice1.status(Invoice.Status.APPROVED));
        invoiceStoragePort.update(anthoInvoice1.status(Invoice.Status.APPROVED));

        final Invoice olivierInvoice2 = billingProfileService.previewInvoice(UserId.of(olivier.user().getId()),
                BillingProfile.Id.of(olivierBillingProfile.getId()),
                olivierRewardsPendingInvoice.subList(3, 5).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());
        final Invoice anthoInvoice2 = billingProfileService.previewInvoice(UserId.of(anthony.user().getId()),
                BillingProfile.Id.of(anthonyBillingProfile.getId()),
                anthoRewardsPendingInvoice.subList(7, 15).stream()
                        .filter(userRewardView -> nonNull(userRewardView.getAmount().getDollarsEquivalent()))
                        .map(userRewardView -> RewardId.of(userRewardView.getId())).toList());
        invoiceStoragePort.update(olivierInvoice2.status(Invoice.Status.APPROVED));
        invoiceStoragePort.update(anthoInvoice2.status(Invoice.Status.APPROVED));
        invoiceIds.addAll(List.of(olivierInvoice1.id().value(),
                olivierInvoice2.id().value(),
                anthoInvoice1.id().value(),
                anthoInvoice2.id().value()));

        // When
        final SearchRewardsResponse searchRewardsResponse = client.post()
                .uri(getApiURI(POST_REWARDS_SEARCH))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                            "invoiceIds": ["%s","%s","%s","%s"]
                            }
                        """.formatted(
                        olivierInvoice1.id().value(),
                        olivierInvoice2.id().value(),
                        anthoInvoice1.id().value(),
                        anthoInvoice2.id().value()
                ))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(SearchRewardsResponse.class)
                .getResponseBody().blockFirst();
        final List<UUID> expectedRewardIds = new ArrayList<>();
        expectedRewardIds.addAll(olivierInvoice1.rewards().stream().map(reward -> reward.id().value()).toList());
        expectedRewardIds.addAll(olivierInvoice2.rewards().stream().map(reward -> reward.id().value()).toList());
        expectedRewardIds.addAll(anthoInvoice1.rewards().stream().map(reward -> reward.id().value()).toList());
        expectedRewardIds.addAll(anthoInvoice2.rewards().stream().map(reward -> reward.id().value()).toList());
        for (SearchRewardItemResponse reward : searchRewardsResponse.getRewards()) {
            assertTrue(expectedRewardIds.contains(reward.getId()));
            assertTrue(List.of(Currency.Code.USDC_STR, Currency.Code.LORDS_STR, Currency.Code.STRK_STR).contains(reward.getMoney().getCurrencyCode()));
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
                          "totalPageNumber": 30,
                          "totalItemNumber": 149,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "rewards": [
                            {
                              "id": "5ae4a031-2676-4a96-8ff3-65a934f06fa9",
                              "billingProfile": {
                                "name": "A My company",
                                "type": "COMPANY",
                                "verificationStatus": "VERIFIED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-AMYCOMPANY-002",
                                "status": "APPROVED"
                              },
                              "status": "PROCESSING",
                              "requestedAt": "2024-03-05T18:59:00Z",
                              "processedAt": null,
                              "githubUrls": [],
                              "recipient": {
                                "login": "AnthonyBuisset",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                              },
                              "project": {
                                "name": "Cairo foundry",
                                "logoUrl": null
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 11.22,
                                "dollarsEquivalent": 22,
                                "conversionRate": null,
                                "currencyCode": "LORDS",
                                "currencyName": "Lords",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "afb0d66f-6ccb-4c72-bfa1-22e8aaac12ec",
                              "billingProfile": {
                                "name": "A My company",
                                "type": "COMPANY",
                                "verificationStatus": "VERIFIED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-AMYCOMPANY-002",
                                "status": "APPROVED"
                              },
                              "status": "PROCESSING",
                              "requestedAt": "2024-03-05T18:57:00Z",
                              "processedAt": null,
                              "githubUrls": [],
                              "recipient": {
                                "login": "AnthonyBuisset",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                              },
                              "project": {
                                "name": "DogGPT",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15366926246018901574.jpg"
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 11.222,
                                "dollarsEquivalent": 322,
                                "conversionRate": null,
                                "currencyCode": "STRK",
                                "currencyName": "StarkNet Token",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "a2cb3b32-921a-48da-af29-a1e038c6c341",
                              "billingProfile": {
                                "name": "O My company",
                                "type": "COMPANY",
                                "verificationStatus": "VERIFIED",
                                "admins": [
                                  {
                                    "login": "ofux",
                                    "name": "Olivier Fuxet",
                                    "email": "olivier.fuxet@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-OMYCOMPANY-002",
                                "status": "APPROVED"
                              },
                              "status": "PROCESSING",
                              "requestedAt": "2024-03-05T18:56:00Z",
                              "processedAt": null,
                              "githubUrls": [],
                              "recipient": {
                                "login": "ofux",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                              },
                              "project": {
                                "name": "kaaper2",
                                "logoUrl": null
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 11522,
                                "dollarsEquivalent": 222,
                                "conversionRate": null,
                                "currencyCode": "STRK",
                                "currencyName": "StarkNet Token",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "e3ab855b-f6c4-485f-8d34-aa6cfb99e2b3",
                              "billingProfile": {
                                "name": "O My company",
                                "type": "COMPANY",
                                "verificationStatus": "VERIFIED",
                                "admins": [
                                  {
                                    "login": "ofux",
                                    "name": "Olivier Fuxet",
                                    "email": "olivier.fuxet@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-OMYCOMPANY-002",
                                "status": "APPROVED"
                              },
                              "status": "PROCESSING",
                              "requestedAt": "2024-03-05T18:55:00Z",
                              "processedAt": null,
                              "githubUrls": [],
                              "recipient": {
                                "login": "ofux",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                              },
                              "project": {
                                "name": "kaaper2",
                                "logoUrl": null
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 171.22,
                                "dollarsEquivalent": 122,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "56a647e2-9ec7-4383-a91a-d41ce899682c",
                              "billingProfile": {
                                "name": "A My company",
                                "type": "COMPANY",
                                "verificationStatus": "VERIFIED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Anthony BUISSET",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                  }
                                ]
                              },
                              "invoice": {
                                "number": "OD-AMYCOMPANY-002",
                                "status": "APPROVED"
                              },
                              "status": "PROCESSING",
                              "requestedAt": "2024-03-05T18:54:00Z",
                              "processedAt": null,
                              "githubUrls": [],
                              "recipient": {
                                "login": "AnthonyBuisset",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                              },
                              "project": {
                                "name": "DogGPT",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15366926246018901574.jpg"
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 11.22,
                                "dollarsEquivalent": 2882,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
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
                          "totalPageNumber": 6,
                          "totalItemNumber": 30,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "rewards": [
                            {
                              "id": "5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c",
                              "billingProfile": {
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:09:31.842962Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
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
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                              "billingProfile": {
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:06:42.730697Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
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
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "64fb2732-5632-4b09-a8b1-217485648129",
                              "billingProfile": {
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-10-08T10:00:31.105159Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
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
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a",
                              "billingProfile": {
                                "id": "7557dc00-6aa3-4a1c-9a00-9bdef3056943",
                                "name": "Test",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Oucif",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-09-20T08:46:52.77875Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1221"
                              ],
                              "recipient": {
                                "login": "PierreOucif",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "project": {
                                "name": "QA new contributions",
                                "logoUrl": null
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
                            },
                            {
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53",
                              "billingProfile": {
                                "id": "7557dc00-6aa3-4a1c-9a00-9bdef3056943",
                                "name": "Test",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "PierreOucif",
                                    "name": "Pierre Oucif",
                                    "email": "pierre.oucif@gadz.org",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "PENDING_VERIFICATION",
                              "requestedAt": "2023-09-19T07:40:26.971981Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1138",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1139",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1157",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1170",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1171",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1178",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1179",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1180",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1183",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1185",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1186",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1187",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1188",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1194",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1195",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1198",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1200",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1203",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1206",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1209",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1220",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1225",
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/1232"
                              ],
                              "recipient": {
                                "login": "PierreOucif",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "project": {
                                "name": "QA new contributions",
                                "logoUrl": null
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": null,
                              "paidTo": null
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
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:48.146947Z",
                              "processedAt": "2023-02-09T07:35:03.828144Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
                              "paidTo": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                            },
                            {
                              "id": "e23ad82b-27c5-4840-9481-da31aef6ba1b",
                              "billingProfile": {
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:40.924453Z",
                              "processedAt": "2023-02-09T07:35:03.417235Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 2500,
                                "dollarsEquivalent": 2525.00,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
                              "paidTo": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                            },
                            {
                              "id": "72f257fa-1b20-433d-9cdd-88d5182b7369",
                              "billingProfile": {
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:31.946777Z",
                              "processedAt": "2023-02-09T07:35:02.985188Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 750,
                                "dollarsEquivalent": 757.50,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
                              "paidTo": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                            },
                            {
                              "id": "91c7e960-37ba-4334-ba91-f1b02f1927ab",
                              "billingProfile": {
                                "id": "4d47beb1-8f63-476b-8548-9f9fe97f0a6c",
                                "name": "OnlyDust",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "gregcha",
                                    "name": "Gregoire Charles",
                                    "email": "gcm.charles@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-09T07:24:20.274391Z",
                              "processedAt": "2023-02-09T07:35:02.582428Z",
                              "githubUrls": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "recipient": {
                                "login": "gregcha",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                              },
                              "project": {
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 1000,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
                              "paidTo": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"
                            },
                            {
                              "id": "e6152967-9bd4-40e6-bad5-c9c4a9578d0f",
                              "billingProfile": {
                                "id": "86295e05-6a91-40ba-8544-6f56c2dbec6e",
                                "name": "sasfd",
                                "type": "COMPANY",
                                "verificationStatus": "NOT_STARTED",
                                "admins": [
                                  {
                                    "login": "oscarwroche",
                                    "name": "sdf sdf",
                                    "email": "oscar.w.roche@gmail.com",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4"
                                  }
                                ]
                              },
                              "invoice": null,
                              "status": "COMPLETE",
                              "requestedAt": "2023-02-08T09:14:56.053584Z",
                              "processedAt": "2023-02-27T11:56:28.044044Z",
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/663"
                              ],
                              "recipient": {
                                "login": "oscarwroche",
                                "name": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4"
                              },
                              "project": {
                                "name": "oscar's awesome project",
                                "logoUrl": null
                              },
                              "sponsors": [],
                              "money": {
                                "amount": 500,
                                "dollarsEquivalent": 505.00,
                                "conversionRate": null,
                                "currencyCode": "USDC",
                                "currencyName": "USD Coin",
                                "currencyLogoUrl": null
                              },
                              "transactionHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
                              "paidTo": "0xd8da6bf26964af9d7eed9e03e53415d37aa96045"
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
                Starklings,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:48.146947Z,2023-02-09T07:35:03.828144Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#BDB59",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Starklings - USDC,1.01,1010.00
                Starklings,Gregoire Charles,gregcha,2500,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:40.924453Z,2023-02-09T07:35:03.417235Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#E23AD",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Starklings - USDC,1.01,2525.00
                Starklings,Gregoire Charles,gregcha,750,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:31.946777Z,2023-02-09T07:35:02.985188Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#72F25",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Starklings - USDC,1.01,757.50
                Starklings,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-02-09T07:24:20.274391Z,2023-02-09T07:35:02.582428Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#91C7E",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Starklings - USDC,1.01,1010.00
                oscar's awesome project,sdf sdf,oscarwroche,500,USDC,[https://github.com/onlydustxyz/marketplace-frontend/pull/663],COMPLETE,2023-02-08T09:14:56.053584Z,2023-02-27T11:56:28.044044Z,0x0000000000000000000000000000000000000000000000000000000000000000,0xd8da6bf26964af9d7eed9e03e53415d37aa96045,"#E6152",[],oscar.w.roche@gmail.com,NOT_STARTED,COMPANY,,,oscar's awesome project - USDC,1.01,505.00
                """);
    }

    @Test
    @Order(6)
    void should_export_all_rewards_between_processed_dates() {
        // When
        final var csv = client.get()
                .uri(getApiURI(GET_REWARDS_CSV, Map.of("statuses", "COMPLETE",
                        "fromProcessedAt", "2023-06-01", "toProcessedAt", "2024-01-31"))
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
                Bretzel,Gregoire Charles,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-26T15:57:29.834949Z,2023-09-26T21:08:01.957813Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#736E0","[Coca Cola, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,Gregoire Charles,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-26T08:43:36.823851Z,2023-09-26T21:08:01.735916Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#1C56D","[Coca Cola, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,Gregoire Charles,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-25T13:12:26.971685Z,2023-09-26T21:08:01.601831Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#4CCF3","[Coca Cola, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,Gregoire Charles,gregcha,1000.00,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-09-25T13:01:35.433511Z,2023-09-26T21:25:50.605546Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#CF65A","[Coca Cola, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Bretzel - USDC,1.0100,1010.0000
                Bretzel,Gregoire Charles,gregcha,1000,USDC,[https://github.com/gregcha/bretzel-app/issues/1],COMPLETE,2023-08-10T14:25:38.310796Z,2023-09-26T21:25:49.941482Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#1CCB3","[Coca Cola, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Bretzel - USDC,1.01,1010.00
                Bretzel,Gregoire Charles,gregcha,1000,USDC,[https://github.com/gregcha/bretzel-site/issues/1],COMPLETE,2023-07-26T10:06:57.034426Z,2023-09-26T21:25:48.826952Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#B69D6","[Coca Cola, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Bretzel - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-07-23T08:34:56.803043Z,2023-09-26T21:28:32.053680Z,OK cool,FR7640618802650004034616528,"#0D951",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USD,1,1000
                Taco Tuesday,Gregoire Charles,gregcha,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-07-06T07:59:45.045849Z,2023-10-06T17:13:39.984503Z,yeaah,FR7640618802650004034616528,"#CF023",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USD,1,1000
                Taco Tuesday,Gregoire Charles,gregcha,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/issues/3],COMPLETE,2023-07-06T07:56:24.591202Z,2023-10-08T09:49:11.213844Z,yeaaaah,FR7640618802650004034616528,"#B0CEB",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USD,1,1000
                Taco Tuesday,Gregoire Charles,gregcha,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-07-06T07:54:27.050353Z,2023-10-06T17:09:10.488656Z,coucou,FR7640618802650004034616528,"#3C906",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USD,1,1000
                Aldébaran du Taureau,Anthony BUISSET,AnthonyBuisset,1000,STRK,"[https://github.com/MaximeBeasse/KeyDecoder/issues/3, https://github.com/MaximeBeasse/KeyDecoder/pull/1]",COMPLETE,2023-06-22T09:37:23.518886Z,2023-07-27T10:27:15.315253Z,0x0000000000000000000000000000000000000000000000000000000000000000,abuisset.eth,"#B31A4","[AS Nancy Lorraine, OGC Nissa Ineos]",abuisset@gmail.com,VERIFIED,COMPANY,,,Aldébaran du Taureau - STRK,,
                Aldébaran du Taureau,Anthony BUISSET,AnthonyBuisset,1750,USDC,"[https://github.com/MaximeBeasse/KeyDecoder/issues/3, https://github.com/MaximeBeasse/KeyDecoder/pull/1]",COMPLETE,2023-06-22T08:47:12.915468Z,2023-07-27T10:27:14.782552Z,0x0000000000000000000000000000000000000000000000000000000000000000,abuisset.eth,"#EE283","[AS Nancy Lorraine, OGC Nissa Ineos]",abuisset@gmail.com,VERIFIED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,1767.50
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-21T16:09:26.565380Z,2023-06-21T16:16:05.542193Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#4745D",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/issues/3],COMPLETE,2023-06-21T16:08:03.421165Z,2023-06-21T16:16:05.501815Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#A4234",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/issues/3],COMPLETE,2023-06-21T16:07:01.585046Z,2023-06-21T16:16:05.462540Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#7B0F6",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/issues/3],COMPLETE,2023-06-21T16:00:44.754809Z,2023-06-21T16:16:05.420146Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#4A23B",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-21T15:56:31.915313Z,2023-06-21T16:16:05.325781Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#6187B",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Aldébaran du Taureau,Gregoire Charles,gregcha,2500,USDC,"[https://github.com/MaximeBeasse/KeyDecoder/issues/3, https://github.com/MaximeBeasse/KeyDecoder/pull/1]",COMPLETE,2023-06-20T10:58:30.275669Z,2023-06-21T15:37:04.235514Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#E75D1","[AS Nancy Lorraine, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,2525.00
                Aldébaran du Taureau,Gregoire Charles,gregcha,1750,USDC,"[https://github.com/MaximeBeasse/KeyDecoder/issues/3, https://github.com/MaximeBeasse/KeyDecoder/pull/1, https://github.com/eFounders/efounders-workable-client/issues/2, https://github.com/od-mocks/cool-repo-A/issues/379]",COMPLETE,2023-06-20T07:42:21.332040Z,2023-06-21T15:37:03.921254Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#420CA","[AS Nancy Lorraine, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,1767.50
                Aldébaran du Taureau,Gregoire Charles,gregcha,5500,USDC,"[https://github.com/Emmykage/portfolio/issues/14, https://github.com/MaximeBeasse/KeyDecoder/issues/3, https://github.com/MaximeBeasse/KeyDecoder/pull/1]",COMPLETE,2023-06-16T17:09:08.048298Z,2023-06-19T21:26:31.162539Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#A4D8E","[AS Nancy Lorraine, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,5555.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-16T16:54:22.119984Z,2023-06-19T21:26:31.119345Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#B9359",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-16T16:43:07.063277Z,2023-06-19T21:26:31.074390Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#74301",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Taco Tuesday,Gregoire Charles,gregcha,1500,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-16T16:40:05.043259Z,2023-06-19T21:26:31.033539Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#7DD21",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1515.00
                Taco Tuesday,Gregoire Charles,gregcha,1750,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-16T16:01:29.786743Z,2023-06-19T21:26:30.983109Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#4C259",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1767.50
                Taco Tuesday,Gregoire Charles,gregcha,1750,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-16T15:57:11.777649Z,2023-06-19T21:26:30.933236Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#600A4",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1767.50
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-06-05T18:06:27.648269Z,2023-06-19T21:26:30.821013Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#34A06",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Pizzeria Yoshi !,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-05-26T09:46:21.215986Z,2023-06-19T21:35:58.329155Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#D55BE",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Pizzeria Yoshi ! - USDC,1.01,1010.00
                Pizzeria Yoshi !,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-05-26T09:45:22.077302Z,2023-06-19T21:35:57.981608Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#F270A",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Pizzeria Yoshi ! - USDC,1.01,1010.00
                Pizzeria Yoshi !,Gregoire Charles,gregcha,1000,USD,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-05-26T09:30:42.881962Z,2023-06-19T21:40:42.314436Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#03413",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Pizzeria Yoshi ! - USD,1,1000
                Taco Tuesday,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-05-22T16:12:59.162216Z,2023-06-21T15:37:03.883232Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#6FDC4",[Red Bull],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Taco Tuesday - USDC,1.01,1010.00
                Pizzeria Yoshi !,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-05-19T16:48:23.145660Z,2023-06-21T15:37:03.838492Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#0A7A4",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Pizzeria Yoshi ! - USDC,1.01,1010.00
                Pizzeria Yoshi !,Anthony BUISSET,AnthonyBuisset,1000,USDC,[https://github.com/od-mocks/cool-repo-A/pull/397],COMPLETE,2023-05-15T12:15:54.255290Z,2023-07-27T10:27:14.522708Z,0x0000000000000000000000000000000000000000000000000000000000000000,abuisset.eth,"#061E2",[],abuisset@gmail.com,VERIFIED,COMPANY,,,Pizzeria Yoshi ! - USDC,1.01,1010.00
                Aldébaran du Taureau,Gregoire Charles,gregcha,1000,USDC,[https://github.com/starknet-id/stats.starknet.id/pull/12],COMPLETE,2023-03-20T10:54:38.467011Z,2023-06-21T15:37:03.803663Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#54007","[AS Nancy Lorraine, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,1010.00
                Aldébaran du Taureau,Gregoire Charles,gregcha,2000,USDC,[https://github.com/starknet-id/stats.starknet.id/pull/12],COMPLETE,2023-03-20T10:54:15.070044Z,2023-06-21T15:37:03.763624Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#5F1F8","[AS Nancy Lorraine, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,2020.00
                Aldébaran du Taureau,Gregoire Charles,gregcha,1000,USDC,[https://github.com/starknet-id/stats.starknet.id/pull/12],COMPLETE,2023-03-20T10:54:06.888272Z,2023-06-21T15:37:03.725796Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#1BF84","[AS Nancy Lorraine, OGC Nissa Ineos]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Aldébaran du Taureau - USDC,1.01,1010.00
                Starklings,Gregoire Charles,gregcha,1000,USDC,[https://github.com/starknet-id/stats.starknet.id/pull/12],COMPLETE,2023-03-20T09:13:36.459128Z,2023-06-21T15:37:03.375232Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#2CEFB",[],gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Starklings - USDC,1.01,1010.00
                Mooooooonlight,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-03-13T17:00:40.161532Z,2023-06-21T15:37:03.332235Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#E4F41","[Starknet Foundation, Theodo]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Mooooooonlight - USDC,1.01,1010.00
                Mooooooonlight,Gregoire Charles,gregcha,1000,USDC,[https://github.com/MaximeBeasse/KeyDecoder/pull/1],COMPLETE,2023-03-13T17:00:21.831113Z,2023-06-21T15:37:03.239054Z,0x0000000000000000000000000000000000000000000000000000000000000000,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#B3AB5","[Starknet Foundation, Theodo]",gcm.charles@gmail.com,NOT_STARTED,COMPANY,,,Mooooooonlight - USDC,1.01,1010.00
                Mooooooonlight,sdf sdf,oscarwroche,1000,USDC,[https://github.com/onlydustxyz/marketplace-frontend/pull/743],COMPLETE,2023-03-01T12:48:51.425766Z,2023-09-26T20:22:12.865097Z,0x61b205c29984b5b2eaec5025e6b24ace49691f458fe0dcb9cbaeeb97186507db,0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea,"#AB855","[Starknet Foundation, Theodo]",oscar.w.roche@gmail.com,NOT_STARTED,COMPANY,,,Mooooooonlight - USDC,1.01,1010.00
                Marketplace 2,Anthony BUISSET,AnthonyBuisset,500,ETH,[https://github.com/onlydustxyz/marketplace-frontend/pull/661],COMPLETE,2023-02-07T17:15:40.383831Z,2023-07-27T10:27:14.168340Z,0x0000000000000000000000000000000000000000000000000000000000000000,abuisset.eth,"#1C06C",[],abuisset@gmail.com,VERIFIED,COMPANY,,,Marketplace 2 - ETH,1781.98,890990.00
                Marketplace 2,Olivier Fuxet,ofux,438,USD,[https://github.com/onlydustxyz/marketplace-frontend/pull/642],COMPLETE,2023-02-02T15:20:35.665817Z,2023-09-26T20:24:00.439566Z,Coucou les filles,GB33BUKB20201555555555,"#C5AE2",[],olivier.fuxet@gmail.com,VERIFIED,COMPANY,,,Marketplace 2 - USD,1,438
                """);
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
                            "invoiceIds": ["%s","%s","%s","%s"]
                            }
                        """.formatted(
                        invoiceIds.get(0),
                        invoiceIds.get(1),
                        invoiceIds.get(2),
                        invoiceIds.get(3)))
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
                              "csv": "erc20,0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0,0x716E30e2981481bc56CCc315171A9E2923bD12B4,11.22\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,11.22\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000",
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
                              "csv": "erc20,0x04718f5a0fc34cc1af16a1cdee98ffb20c31f5cd61d6ab07201858f4287c938d,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7,11.222\\nerc20,0x04718f5a0fc34cc1af16a1cdee98ffb20c31f5cd61d6ab07201858f4287c938d,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc8,11522",
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


    @Test
    @Order(103)
    void should_pay_strk_batch_payment() {
        // Given
        final var toto = batchPaymentRepository.findAll();
        final BatchPaymentEntity starknetBatchPaymentEntity = batchPaymentRepository.findAll().stream()
                .filter(batchPaymentEntity -> batchPaymentEntity.getNetwork().equals(NetworkEnumEntity.starknet))
                .findFirst()
                .orElseThrow();
        final String transactionHash = "0x" + faker.random().hex();

        final PaymentRequestEntity r1 = paymentRequestRepository.findById(starknetBatchPaymentEntity.getRewardIds().get(0)).orElseThrow();
        final PaymentRequestEntity r2 = paymentRequestRepository.findById(starknetBatchPaymentEntity.getRewardIds().get(1)).orElseThrow();


        rustApiWireMockServer.stubFor(
                WireMock.post("/api/payments/%s/receipts".formatted(r1.getId()))
                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
                        .withRequestBody(WireMock.equalToJson(
                                """
                                        {
                                           "amount": %s,
                                           "currency": "%s",
                                           "recipientWallet": "%s",
                                           "recipientIban" : null,
                                           "transactionReference" : "%s"
                                        }
                                            """.formatted(r1.getAmount().toString(), r1.getCurrency().toDomain().name(), anthoStarknetAddress, transactionHash)
                        ))
                        .willReturn(ResponseDefinitionBuilder.okForJson("""
                                {
                                    "receipt_id": "%s"
                                }""".formatted(UUID.randomUUID()))));

        rustApiWireMockServer.stubFor(
                WireMock.post("/api/payments/%s/receipts".formatted(r2.getId()))
                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
                        .withRequestBody(WireMock.equalToJson(
                                """
                                        {
                                           "amount": %s,
                                           "currency": "%s",
                                           "recipientWallet": "%s",
                                           "recipientIban" : null,
                                           "transactionReference" : "%s"
                                        }
                                        """.formatted(r2.getAmount().toString(), r2.getCurrency().toDomain().name(), olivierStarknetAddress,
                                        transactionHash)
                        ))
                        .willReturn(ResponseDefinitionBuilder.okForJson("""
                                {
                                    "receipt_id": "%s"
                                }""".formatted(UUID.randomUUID()))));


        // When
        client.put()
                .uri(getApiURI(PUT_REWARDS_BATCH_PAYMENTS.formatted(starknetBatchPaymentEntity.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Api-Key", apiKey())
                .bodyValue("""
                                          {
                                            "transactionHash": "%s"
                                          }
                        """.formatted(transactionHash))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        final BatchPayment batchPayment = batchPaymentRepository.findById(starknetBatchPaymentEntity.getId()).orElseThrow().toDomain();
        assertEquals(BatchPayment.Status.PAID, batchPayment.status());
        assertTrue(batchPayment.rewardIds().contains(RewardId.of(r1.getId())));
        assertTrue(batchPayment.rewardIds().contains(RewardId.of(r2.getId())));
    }


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
