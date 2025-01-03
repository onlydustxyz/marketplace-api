package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.InvoiceInnerData;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ReceiptEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountingHelper {

    protected static final Faker faker = new Faker();

    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    RewardStatusRepository rewardStatusRepository;
    @Autowired
    InvoiceRewardRepository invoiceRewardRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    BillingProfileRepository billingProfileRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    HistoricalQuoteRepository historicalQuoteRepository;
    @Autowired
    LatestQuoteRepository latestQuoteRepository;
    @Autowired
    OldestQuoteRepository oldestQuoteRepository;
    @Autowired
    QuoteStorage quoteStorage;
    @Autowired
    AccountingFacadePort accountingFacadePort;
    @Autowired
    PostgresBiProjectorAdapter biProjectorAdapter;

    public CurrencyEntity strk() {
        return currencyRepository.findByCode("STRK").orElseThrow();
    }

    public CurrencyEntity usdc() {
        return currencyRepository.findByCode("USDC").orElseThrow();
    }

    public CurrencyEntity usd() {
        return currencyRepository.findByCode("USD").orElseThrow();
    }

    public CurrencyEntity op() {
        return currencyRepository.findByCode("OP").orElseThrow();
    }

    public CurrencyEntity apt() {
        return currencyRepository.findByCode("APT").orElseThrow();
    }

    public CurrencyEntity lords() {
        return currencyRepository.findByCode("LORDS").orElseThrow();
    }

    @SneakyThrows
    @Transactional
    public void patchReward(@NonNull String id, Number amount, String currencyCode, Number usdAmount, String invoiceReceivedAt, String paidAt) {
        final var rewardEntity = rewardRepository.findById(UUID.fromString(id)).orElseThrow();
        final var rewardStatus = rewardStatusRepository.findById(rewardEntity.id()).orElseThrow();

        if (amount != null) rewardEntity.amount(BigDecimal.valueOf(amount.doubleValue()));
        if (currencyCode != null) {
            final var currency = currencyRepository.findByCode(currencyCode).orElseThrow();
            final var network = switch (currencyCode) {
                case "ETH", "USDC" -> NetworkEnumEntity.ETHEREUM;
                case "APT" -> NetworkEnumEntity.APTOS;
                case "OP" -> NetworkEnumEntity.OPTIMISM;
                case "STRK" -> NetworkEnumEntity.STARKNET;
                default -> throw new IllegalArgumentException("Currency code %s not mapped".formatted(currencyCode));
            };

            rewardEntity.currencyId(currency.id());
            rewardStatus.networks(new NetworkEnumEntity[]{network});
        }
        rewardStatus.amountUsdEquivalent(usdAmount == null ? null : BigDecimal.valueOf(usdAmount.doubleValue()));
        rewardStatus.usdConversionRate(usdAmount == null ? null : BigDecimal.valueOf(usdAmount.doubleValue()).divide(rewardEntity.amount(),
                RoundingMode.HALF_EVEN));

        if (invoiceReceivedAt != null) {
            final var invoiceEntity = fakeInvoice(UUID.randomUUID(), List.of(rewardEntity.id()));
            rewardEntity.invoice(invoiceEntity);
            rewardStatus.invoiceReceivedAt(new SimpleDateFormat("yyyy-MM-dd").parse(invoiceReceivedAt));
        }

        if (paidAt != null) {
            rewardStatus.paidAt(new SimpleDateFormat("yyyy-MM-dd").parse(paidAt));
        }

        rewardRepository.save(rewardEntity);
        rewardStatusRepository.save(rewardStatus);
    }

    @SneakyThrows
    @Transactional
    public void addInvoice(@NonNull UUID rewardId, @NonNull UUID invoiceId, @NonNull Date invoiceReceivedAt) {
        final var rewardEntity = rewardRepository.findById(rewardId).orElseThrow();
        final var rewardStatus = rewardStatusRepository.findById(rewardId).orElseThrow();

        final var invoiceEntity = fakeInvoice(invoiceId, List.of(rewardEntity.id()));
        rewardEntity.invoice(invoiceEntity);
        rewardStatus.invoiceReceivedAt(invoiceReceivedAt);

        rewardRepository.save(rewardEntity);
        rewardStatusRepository.save(rewardStatus);
    }

    @SneakyThrows
    @Transactional
    public void setPaid(@NonNull UUID rewardId, @NonNull Date paidAt, @NonNull Payment.Reference transactionReference) {
        final var rewardEntity = rewardRepository.findById(rewardId).orElseThrow();
        final var rewardStatus = rewardStatusRepository.findById(rewardId).orElseThrow();

        rewardStatus.paidAt(paidAt);
        rewardEntity.receipts().add(ReceiptEntity.of(Receipt.of(RewardId.of(rewardId), transactionReference)));

        rewardRepository.save(rewardEntity);
        rewardStatusRepository.save(rewardStatus);
        biProjectorAdapter.onRewardPaid(RewardId.of(rewardId));
    }

    @SneakyThrows
    @Transactional
    public InvoiceEntity fakeInvoice(UUID id, List<UUID> rewardIds) {
        final var firstName = faker.name().firstName();
        final var lastName = faker.name().lastName();

        final var rewards = invoiceRewardRepository.findAll(rewardIds);
        final var innerData = new InvoiceInnerData(
                ZonedDateTime.now().plusDays(9),
                BigDecimal.ZERO,
                new Invoice.BillingProfileSnapshot(
                        BillingProfile.Id.random(),
                        BillingProfile.Type.INDIVIDUAL,
                        new Invoice.BillingProfileSnapshot.KycSnapshot(
                                firstName,
                                lastName,
                                faker.address().fullAddress(),
                                faker.address().countryCode(),
                                false
                        ),
                        null,
                        null,
                        List.of(new Wallet(Network.ETHEREUM, "vitalik.eth"))
                ),
                null
        );

        return new InvoiceEntity(
                id,
                UUID.randomUUID(),
                Invoice.Number.of(12, lastName, firstName).toString(),
                UUID.randomUUID(),
                ZonedDateTime.now().minusDays(1),
                Invoice.Status.TO_REVIEW,
                rewards.stream().map(InvoiceRewardEntity::targetAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add),
                rewards.get(0).targetCurrency().id(),
                new URL("https://s3.storage.com/invoice.pdf"),
                null,
                null,
                innerData
        );
    }

    public void patchBillingProfile(@NonNull UUID billingProfileId,
                                    BillingProfile.Type type,
                                    VerificationStatus status) {

        final var billingProfile = billingProfileRepository.findById(billingProfileId).orElseThrow();

        if (type != null) billingProfile.setType(type);

        if (status != null) {
            if (billingProfile.getKyb() != null) {
                billingProfile.getKyb().verificationStatus(status);
                kybRepository.save(billingProfile.getKyb());
            } else {
                billingProfile.getKyc().verificationStatus(status);
                kycRepository.save(billingProfile.getKyc());
            }
            billingProfile.setVerificationStatus(status);
        }

        billingProfileRepository.save(billingProfile);
    }

    public void clearAllQuotes() {
        historicalQuoteRepository.deleteAll();
        latestQuoteRepository.deleteAll();
        oldestQuoteRepository.deleteAll();
    }

    public void saveQuote(Quote quote) {
        quoteStorage.save(List.of(quote));
    }

    public SponsorAccount.Id createSponsorAccount(final @NonNull SponsorId sponsorId, final long amount, final @NonNull Currency.Id currencyId) {
        return accountingFacadePort.createSponsorAccountWithInitialAllowance(sponsorId, currencyId, null, PositiveAmount.of(amount))
                .account().id();
    }

    public void allocate(SponsorId sponsorId, ProgramId programId, long amount, Currency.Id currencyId) {
        accountingFacadePort.allocate(sponsorId, programId, PositiveAmount.of(amount), currencyId);
    }

    public void allocate(SponsorId sponsorId, ProgramId programId, double amount, Currency.Id currencyId) {
        accountingFacadePort.allocate(sponsorId, programId, PositiveAmount.of(amount), currencyId);
    }

    public void unallocate(ProgramId programId, SponsorId sponsorId, long amount, Currency.Id currencyId) {
        accountingFacadePort.unallocate(programId, sponsorId, PositiveAmount.of(amount), currencyId);
    }

    public void unallocate(ProgramId programId, SponsorId sponsorId, double amount, Currency.Id currencyId) {
        accountingFacadePort.unallocate(programId, sponsorId, PositiveAmount.of(amount), currencyId);
    }

    public void grant(ProgramId programId, ProjectId projectId, long amount, Currency.Id currencyId) {
        accountingFacadePort.grant(programId, projectId, PositiveAmount.of(amount), currencyId);
    }

    public void grant(ProgramId programId, ProjectId projectId, double amount, Currency.Id currencyId) {
        accountingFacadePort.grant(programId, projectId, PositiveAmount.of(amount), currencyId);
    }

    public void pay(RewardId... rewardIds) {
        accountingFacadePort.pay(Set.of(rewardIds));
    }

    public void ungrant(ProjectId projectId, ProgramId programId, long amount, Currency.Id currencyId) {
        accountingFacadePort.ungrant(projectId, programId, PositiveAmount.of(amount), currencyId);
    }

    public void ungrant(ProjectId projectId, ProgramId programId, double amount, Currency.Id currencyId) {
        accountingFacadePort.ungrant(projectId, programId, PositiveAmount.of(amount), currencyId);
    }

    public void approve(Deposit.Id depositId) {
        accountingFacadePort.approveDeposit(depositId);
    }
}
