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
                                "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                                "name": "A My company",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                                "name": "A My company",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "id": "a59fd44b-f75f-430b-b60b-a4550c7feae4",
                                "name": "O My company",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "id": "a59fd44b-f75f-430b-b60b-a4550c7feae4",
                                "name": "O My company",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                                "name": "A My company",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                                "verificationStatus": null,
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
                              "csv": "erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7,11.222\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc8,11522",
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
