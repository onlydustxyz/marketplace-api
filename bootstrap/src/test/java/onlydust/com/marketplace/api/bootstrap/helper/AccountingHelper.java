package onlydust.com.marketplace.api.bootstrap.helper;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    InvoiceRepository invoiceRepository;
    @Autowired
    BillingProfileRepository billingProfileRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    KycRepository kycRepository;

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
    public void patchReward(@NonNull String id, Number amount, String currencyCode, Number usdAmount, String invoiceReceivedAt, String paidAt) {
        final var rewardEntity = rewardRepository.findById(UUID.fromString(id)).orElseThrow();
        final var rewardStatus = rewardStatusRepository.findById(rewardEntity.id()).orElseThrow();

        if (amount != null) rewardEntity.amount(BigDecimal.valueOf(amount.doubleValue()));
        if (currencyCode != null) {
            final var currency = currencyRepository.findByCode(currencyCode).orElseThrow();
            final var network = switch (currencyCode) {
                case "ETH", "USDC" -> NetworkEnumEntity.ethereum;
                case "APT" -> NetworkEnumEntity.aptos;
                case "OP" -> NetworkEnumEntity.optimism;
                case "STRK" -> NetworkEnumEntity.starknet;
                default -> throw new IllegalArgumentException("Currency code %s not mapped".formatted(currencyCode));
            };

            rewardEntity.currencyId(currency.id());
            rewardStatus.networks(new NetworkEnumEntity[]{network});
        }
        rewardStatus.amountUsdEquivalent(usdAmount == null ? null : BigDecimal.valueOf(usdAmount.doubleValue()));

        if (invoiceReceivedAt != null) {
            final var invoiceEntity = fakeInvoice(UUID.randomUUID(), List.of(rewardEntity.id()));
            invoiceRepository.save(invoiceEntity);
            rewardEntity.invoiceId(invoiceEntity.id());
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
    public InvoiceEntity fakeInvoice(UUID id, List<UUID> rewardIds) {
        final var firstName = faker.name().firstName();
        final var lastName = faker.name().lastName();

        final var rewards = invoiceRewardRepository.findAll(rewardIds);

        return new InvoiceEntity(
                id,
                UUID.randomUUID(),
                Invoice.Number.of(12, lastName, firstName).toString(),
                UUID.randomUUID(),
                ZonedDateTime.now().minusDays(1),
                InvoiceEntity.Status.TO_REVIEW,
                rewards.stream().map(InvoiceRewardEntity::targetAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add),
                rewards.get(0).targetCurrency().id(),
                new URL("https://s3.storage.com/invoice.pdf"),
                null,
                null,
                new InvoiceEntity.Data(
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
                        rewards
                )
        );
    }

    public void patchBillingProfile(@NonNull UUID billingProfileId,
                                    BillingProfileEntity.Type type,
                                    VerificationStatusEntity status) {

        final var billingProfile = billingProfileRepository.findById(billingProfileId).orElseThrow();

        if (type != null) billingProfile.setType(type);

        if (status != null) {
            if (billingProfile.getKyb() != null) {
                billingProfile.getKyb().verificationStatus(status);
                kybRepository.save(billingProfile.getKyb());
            } else {
                billingProfile.getKyc().setVerificationStatus(status);
                kycRepository.save(billingProfile.getKyc());
            }
            billingProfile.setVerificationStatus(status);
        }

        billingProfileRepository.save(billingProfile);
    }
}
