package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.SearchRewardItemResponse;
import onlydust.com.backoffice.api.contract.model.SearchRewardsResponse;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldVerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.project.domain.model.OldAccountNumber;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.project.domain.service.UserService;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    List<UUID> invoiceIds = new ArrayList<>();
    List<UUID> rewardIds = new ArrayList<>();

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
                .starknetAddress(new StarknetAccountAddress("0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7"))
                .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                        .accountNumber(OldAccountNumber.of("FR24 1009 6000 4032 5458 9765 X13"))
                        .bic("BOUSFRPPXXX")
                        .build())
                .build());

        userService.updatePayoutSettings(anthony.user().getId(), UserPayoutSettings.builder()
                .ethWallet(new Wallet(new EvmAccountAddress("0x716E30e2981481bc56CCc315171A9E2923bD12B4")))
                .aptosAddress(new AptosAccountAddress("0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5"))
                .optimismAddress(new EvmAccountAddress("0x716E30e2981481bc56CCc315171A9E2923bD12B4"))
                .starknetAddress(new StarknetAccountAddress("0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7"))
                .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                        .accountNumber(OldAccountNumber.of("FR24 1009 6000 4032 5458 9765 X13"))
                        .bic("BOUSFRPPXXX")
                        .build())
                .build());

        final List<ProjectEntity> projects = projectRepository.findAll();
        paymentRequestRepository.saveAll(List.of(
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(0).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(22)),
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(1).getId(), CurrencyEnumEntity.op, BigDecimal.valueOf(2212)),
                new PaymentRequestEntity(UUID.randomUUID(), olivier.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.222),
                        null, 0, projects.get(2).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(322)),
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), olivier.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11522),
                        null, 0, projects.get(3).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(222)),
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), olivier.user().getGithubUserId(), new Date(), BigDecimal.valueOf(171.22),
                        null, 0, projects.get(3).getId(), CurrencyEnumEntity.usd, BigDecimal.valueOf(122)),
                new PaymentRequestEntity(UUID.randomUUID(), olivier.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(2).getId(), CurrencyEnumEntity.apt, BigDecimal.valueOf(2882))
        ));

        final List<UserRewardView> olivierRewardsPendingInvoice = userService.getPendingInvoiceRewardsForRecipientId(olivier.user().getGithubUserId());
        final List<UserRewardView> anthoRewardsPendingInvoice = userService.getPendingInvoiceRewardsForRecipientId(anthony.user().getGithubUserId());

        final Invoice olivierInvoice1 = billingProfileService.previewInvoice(UserId.of(olivier.user().getId()),
                BillingProfile.Id.of(olivierBillingProfile.getId()),
                olivierRewardsPendingInvoice.subList(0, 2).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());

        final Invoice anthoInvoice1 = billingProfileService.previewInvoice(UserId.of(anthony.user().getId()),
                BillingProfile.Id.of(anthonyBillingProfile.getId()),
                anthoRewardsPendingInvoice.subList(0, 4).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());

        invoiceStoragePort.update(olivierInvoice1.status(Invoice.Status.APPROVED));
        invoiceStoragePort.update(anthoInvoice1.status(Invoice.Status.APPROVED));

        final Invoice olivierInvoice2 = billingProfileService.previewInvoice(UserId.of(olivier.user().getId()),
                BillingProfile.Id.of(olivierBillingProfile.getId()),
                olivierRewardsPendingInvoice.subList(3, 4).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());
        final Invoice anthoInvoice2 = billingProfileService.previewInvoice(UserId.of(anthony.user().getId()),
                BillingProfile.Id.of(anthonyBillingProfile.getId()),
                anthoRewardsPendingInvoice.subList(5, 9).stream().map(userRewardView -> RewardId.of(userRewardView.getId())).toList());
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
        rewardIds.addAll(expectedRewardIds);
        for (SearchRewardItemResponse reward : searchRewardsResponse.getRewards()) {
            Assertions.assertTrue(expectedRewardIds.contains(reward.getId()));
        }

        client.post()
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
                .expectBody()
                .json(SEARCH_REWARDS_FOR_INVOICE_IDS_RESPONSE_JSON);
    }


    private final static String SEARCH_REWARDS_FOR_INVOICE_IDS_RESPONSE_JSON = """
            {
              "rewards": [
                {
                  "id": "0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-09-20T08:00:46.580407Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1026",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1037",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1038",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1039",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1040",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1041",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                },
                {
                  "id": "79209029-c488-4284-aa3f-bce8870d3a66",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-06-02T08:49:08.444047Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/issues/1036"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                },
                {
                  "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-09-20T07:59:16.657487Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                },
                {
                  "id": "966cd55c-7de8-45c4-8bba-b388c38ca15d",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-06-02T08:48:04.697886Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/issues/1034"
                  ],
                  "project": {
                    "name": "kaaper",
                    "logoUrl": null
                  },
                  "sponsors": [],
                  "money": {
                    "amount": 1000,
                    "dollarsEquivalent": 1781980.00,
                    "conversionRate": null,
                    "currencyCode": "ETH",
                    "currencyName": "Ether",
                    "currencyLogoUrl": null
                  }
                },
                {
                  "id": "5c668b61-e42c-4f0e-b31f-44c4e50dc2f4",
                  "billingProfile": {
                    "id": "a59fd44b-f75f-430b-b60b-a4550c7feae4",
                    "name": "O My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "ofux",
                        "name": "Olivier Fuxet",
                        "email": "olivier.fuxet@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-03-20T12:33:11.124316Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/818"
                  ],
                  "project": {
                    "name": "Zero title 5",
                    "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png"
                  },
                  "sponsors": [],
                  "money": {
                    "amount": 1250,
                    "dollarsEquivalent": 1250,
                    "conversionRate": null,
                    "currencyCode": "USD",
                    "currencyName": "US Dollar",
                    "currencyLogoUrl": null
                  }
                },
                {
                  "id": "e33ea956-d2f5-496b-acf9-e2350faddb16",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-09-20T08:01:16.850492Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1026",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1037",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1038",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1039",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1040",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1041",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                },
                {
                  "id": "95e079c9-609c-4531-8c5c-13217306b299",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-09-20T08:02:18.711143Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1026",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1037",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1038",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1039",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1040",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1041",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                },
                {
                  "id": "1fad9f3b-67ab-4499-a320-d719a986d933",
                  "billingProfile": {
                    "id": "a59fd44b-f75f-430b-b60b-a4550c7feae4",
                    "name": "O My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "ofux",
                        "name": "Olivier Fuxet",
                        "email": "olivier.fuxet@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-03-20T13:29:48.908559Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/818"
                  ],
                  "project": {
                    "name": "Zero title 5",
                    "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png"
                  },
                  "sponsors": [],
                  "money": {
                    "amount": 1500,
                    "dollarsEquivalent": 1500,
                    "conversionRate": null,
                    "currencyCode": "USD",
                    "currencyName": "US Dollar",
                    "currencyLogoUrl": null
                  }
                },
                {
                  "id": "6587511b-3791-47c6-8430-8f793606c63a",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-09-20T08:01:47.616674Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1026",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1037",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1038",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1039",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1040",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1041",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                },
                {
                  "id": "dd7d445f-6915-4955-9bae-078173627b05",
                  "billingProfile": {
                    "id": "7aadc53b-d146-4322-8ae2-1ee0083255b3",
                    "name": "A My company",
                    "type": "COMPANY",
                    "admins": [
                      {
                        "login": "AnthonyBuisset",
                        "name": "Anthony BUISSET",
                        "email": "abuisset@gmail.com",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                      }
                    ]
                  },
                  "requestedAt": "2023-09-20T07:59:47.012001Z",
                  "processedAt": null,
                  "githubUrls": [
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                  ],
                  "project": {
                    "name": "kaaper",
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
                  }
                }
              ]
            }
            """;
}
