package onlydust.com.marketplace.api.bootstrap.it.api;

import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.suites.tags.TagConcurrency;
import onlydust.com.marketplace.api.contract.model.InvoicePreviewResponse;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.bootstrap.helper.ConcurrentTesting.runConcurrently;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@TagConcurrency
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoicesApiConcurrencyIT extends AbstractMarketplaceApiIT {
    @Autowired
    PdfStoragePort pdfStoragePort;
    @Autowired
    GlobalSettingsRepository globalSettingsRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    BillingProfileFacadePort billingProfileFacadePort;
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    @Autowired
    RewardRepository rewardRepository;

    UserAuthHelper.AuthenticatedUser antho;
    UUID companyBillingProfileId;

    private static final ProjectId PROJECT_ID = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");
    private static final List<UUID> REWARD_IDS = List.of(
            UUID.fromString("6587511b-3791-47c6-8430-8f793606c63a"),
            UUID.fromString("79209029-c488-4284-aa3f-bce8870d3a66"),
            UUID.fromString("303f26b1-63f0-41f1-ab11-e70b54ef4a2a"),
            UUID.fromString("0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf"),
            UUID.fromString("dd7d445f-6915-4955-9bae-078173627b05"),
            UUID.fromString("d22f75ab-d9f5-4dc6-9a85-60dcd7452028"),
            UUID.fromString("95e079c9-609c-4531-8c5c-13217306b299")
    );

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAnthony();
        companyBillingProfileId = initBillingProfile(antho).value();

        payoutPreferenceFacadePort.setPayoutPreference(PROJECT_ID, BillingProfile.Id.of(companyBillingProfileId), UserId.of(antho.user().getId()));
    }

    private BillingProfile.Id initBillingProfile(UserAuthHelper.AuthenticatedUser owner) {
        final var ownerId = UserId.of(owner.user().getId());

        return billingProfileStoragePort.findAllBillingProfilesForUser(ownerId).stream()
                .filter(bp -> bp.getType() == BillingProfile.Type.COMPANY)
                .findFirst()
                .map(ShortBillingProfileView::getId)
                .orElseGet(() -> createCompanyBillingProfileFor(ownerId).id());
    }

    private CompanyBillingProfile createCompanyBillingProfileFor(UserId ownerId) {
        final var billingProfile = billingProfileFacadePort.createCompanyBillingProfile(ownerId, "My billing profile", Set.of(PROJECT_ID));

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("abuisset.eth"))
                .build(), billingProfile.id());

        billingProfileStoragePort.saveKyb(billingProfile.kyb().toBuilder()
                .name("My company")
                .country(Country.fromIso3("FRA"))
                .address("My address")
                .registrationNumber("123456")
                .subjectToEuropeVAT(true)
                .usEntity(false)
                .status(VerificationStatus.VERIFIED)
                .build());

        billingProfileStoragePort.updateBillingProfileStatus(billingProfile.id(), VerificationStatus.VERIFIED);

        return billingProfile;
    }


    @SneakyThrows
    @Test
    @Order(1)
    void preview_invoices_concurrently() {

        final var rawResponses = new ConcurrentHashMap<Integer, InvoicePreviewResponse>();

        runConcurrently(50, threadId -> {
            // When
            client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                            "rewardIds", REWARD_IDS.stream().map(UUID::toString).collect(Collectors.joining(","))
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    .returnResult(InvoicePreviewResponse.class)
                    .consumeWith(result -> {
                        if (result.getStatus().is2xxSuccessful()) {
                            final var body = result.getResponseBody().blockFirst();
                            rawResponses.put(threadId, body);
                        }
                    });

        });

        assertThat(rawResponses.values()).isNotEmpty();
        final var invoiceIds = rawResponses.values().stream().map(r -> r.getId()).collect(Collectors.toSet());

        // Assert that all rewards have the same invoice id, and that it is one of the invoice ids returned by the API
        final var rewardInvoiceIds = REWARD_IDS.stream().map(rewardId -> rewardRepository.findById(rewardId).orElseThrow().invoiceId()).collect(Collectors.toSet());
        assertThat(rewardInvoiceIds).hasSize(1);
        assertThat(invoiceIds).containsAll(rewardInvoiceIds);
    }

}
