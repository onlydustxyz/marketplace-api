package onlydust.com.marketplace.api.bootstrap.it.bo;

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

import static java.util.Objects.nonNull;

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
    static List<UUID> rewardIds = new ArrayList<>();

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
                        null, 0, projects.get(0).getId(), CurrencyEnumEntity.lords, BigDecimal.valueOf(22)),
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.22),
                        null, 0, projects.get(1).getId(), CurrencyEnumEntity.op, BigDecimal.valueOf(2212)),
                new PaymentRequestEntity(UUID.randomUUID(), olivier.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.222),
                        null, 0, projects.get(2).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(322)),
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), olivier.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11522),
                        null, 0, projects.get(3).getId(), CurrencyEnumEntity.strk, BigDecimal.valueOf(222)),
                new PaymentRequestEntity(UUID.randomUUID(), anthony.user().getId(), olivier.user().getGithubUserId(), new Date(), BigDecimal.valueOf(171.22),
                        null, 0, projects.get(3).getId(), CurrencyEnumEntity.usd, BigDecimal.valueOf(122)),
                new PaymentRequestEntity(UUID.randomUUID(), olivier.user().getId(), anthony.user().getGithubUserId(), new Date(), BigDecimal.valueOf(11.22),
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
        rewardIds.addAll(expectedRewardIds);
        Assertions.assertEquals(expectedRewardIds.size(), searchRewardsResponse.getRewards().size());
    }

    @Test
    @Order(2)
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
                              "csv": "native,,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0,0x716E30e2981481bc56CCc315171A9E2923bD12B4,11.22\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,11.22\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x716E30e2981481bc56CCc315171A9E2923bD12B4,1000",
                              "blockchain": "ETHEREUM",
                              "rewardCount": 13,
                              "totalAmounts": [
                                {
                                  "amount": 1000,
                                  "dollarsEquivalent": 1781980.00,
                                  "conversionRate": null,
                                  "currencyCode": "ETH",
                                  "currencyName": "Ether",
                                  "currencyLogoUrl": null
                                },
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
                              "csv": "erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7,11.222\\nerc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7,11522",
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
                        }
                        """);
    }

}
