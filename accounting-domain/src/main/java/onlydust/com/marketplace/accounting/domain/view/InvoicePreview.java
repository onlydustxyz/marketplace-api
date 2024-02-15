package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Data
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoicePreview {
    private @NonNull ZonedDateTime createdAt = ZonedDateTime.now();
    private @NonNull Integer sequenceNumber;
    private PersonalInfo personalInfo;
    private CompanyInfo companyInfo;
    private BankAccount bankAccount;
    private @NonNull List<Wallet> wallets = new ArrayList<>();
    private @NonNull List<Reward> rewards = new ArrayList<>();

    public static InvoicePreview of(Integer sequenceNumber, PersonalInfo personalInfo) {
        return new InvoicePreview(sequenceNumber).personalInfo(personalInfo);
    }

    public static InvoicePreview of(Integer sequenceNumber, CompanyInfo companyInfo) {
        return new InvoicePreview(sequenceNumber).companyInfo(companyInfo);
    }

    public Invoice.Id id() {
        return switch (billingProfileType()) {
            case INDIVIDUAL -> Invoice.Id.of(sequenceNumber, personalInfo.lastName, personalInfo.firstName);
            case COMPANY -> Invoice.Id.of(sequenceNumber, companyInfo.name);
        };
    }

    public BillingProfile.Type billingProfileType() {
        return personalInfo != null ? BillingProfile.Type.INDIVIDUAL : BillingProfile.Type.COMPANY;
    }

    public ZonedDateTime dueAt() {
        return createdAt.plusDays(10);
    }

    public BigDecimal taxRate() {
        return companyInfo()
                .filter(info -> info.vatRegulationState() == VatRegulationState.APPLICABLE)
                .map(c -> BigDecimal.valueOf(0.2))
                .orElse(BigDecimal.ZERO);
    }

    public Money totalBeforeTax() {
        return rewards.stream().map(Reward::base).reduce(Money::add)
                .orElseThrow(() -> notFound("No reward found for invoice %s".formatted(id())));
    }

    public Money totalTax() {
        return totalBeforeTax().multiply(taxRate());
    }

    public Money totalAfterTax() {
        return totalBeforeTax().add(totalTax());
    }

    public Optional<PersonalInfo> personalInfo() {
        return Optional.ofNullable(personalInfo);
    }

    public Optional<CompanyInfo> companyInfo() {
        return Optional.ofNullable(companyInfo);
    }

    public Optional<BankAccount> bankAccount() {
        return Optional.ofNullable(bankAccount);
    }

    public enum VatRegulationState {
        APPLICABLE, NOT_APPLICABLE_NON_UE, NOT_APPLICABLE_FRENCH_NOT_SUBJECT, REVERSE_CHARGE
    }

    public record Wallet(@NonNull String network, @NonNull String address) {
    }

    public record BankAccount(@NonNull String bic, @NonNull String accountNumber) {
    }

    public record PersonalInfo(@NonNull String firstName, @NonNull String lastName, @NonNull String address) {
    }

    public record CompanyInfo(@NonNull String registrationNumber,
                              @NonNull String name,
                              @NonNull String address,
                              @NonNull Boolean subjectToEuVAT,
                              String euVATNumber
    ) {
        public VatRegulationState vatRegulationState() {
            // TODO
            return subjectToEuVAT ? VatRegulationState.APPLICABLE : VatRegulationState.NOT_APPLICABLE_NON_UE;
        }
    }

    public record Reward(@NonNull RewardId id, @NonNull ZonedDateTime createdAt, @NonNull String projectName, @NonNull Money amount, @NonNull Money base) {
    }
}
