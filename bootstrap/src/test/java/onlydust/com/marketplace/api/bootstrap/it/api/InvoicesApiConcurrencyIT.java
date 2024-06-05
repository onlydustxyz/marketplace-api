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
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

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
            UUID.fromString("0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf")
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
        // When
        final var invoiceId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                        "rewardIds", REWARD_IDS.stream().map(UUID::toString).collect(Collectors.joining(","))
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(invoiceId::setValue)
                .jsonPath("$.createdAt").isNotEmpty()
                .jsonPath("$.dueAt").isNotEmpty()
                .jsonPath("$.rewards.length()").isEqualTo(4);

        REWARD_IDS.forEach(rewardId -> {
            final var reward = rewardRepository.findById(rewardId).orElseThrow();
            assertThat(reward.invoiceId().toString()).isEqualTo(invoiceId.getValue());
        });
    }

}
