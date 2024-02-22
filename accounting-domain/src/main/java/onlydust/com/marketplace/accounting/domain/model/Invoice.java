package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Data
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Invoice {
    public static final int DUE_DAY_COUNT_AFTER_CREATION = 10;

    private final @NonNull Id id;
    private final @NonNull BillingProfile.Id billingProfileId;
    private final @NonNull ZonedDateTime createdAt;
    private final @NonNull ZonedDateTime dueAt;
    private final @NonNull Invoice.Number number;
    private @NonNull Status status;
    private final @NonNull BigDecimal taxRate;
    private PersonalInfo personalInfo;
    private CompanyInfo companyInfo;
    private BankAccount bankAccount;
    private @NonNull List<Wallet> wallets = new ArrayList<>();
    private @NonNull List<Reward> rewards = new ArrayList<>();
    private URL url;
    private String originalFileName;

    public static Invoice of(BillingProfile.Id billingProfileId, Integer sequenceNumber, PersonalInfo personalInfo) {
        final var now = ZonedDateTime.now();
        return new Invoice(
                Id.random(),
                billingProfileId,
                now,
                now.plusDays(DUE_DAY_COUNT_AFTER_CREATION),
                Number.of(sequenceNumber, personalInfo.lastName, personalInfo.firstName),
                Status.DRAFT,
                BigDecimal.ZERO
        ).personalInfo(personalInfo);
    }

    public static Invoice of(BillingProfile.Id billingProfileId, Integer sequenceNumber, CompanyInfo companyInfo) {
        final var now = ZonedDateTime.now();
        return new Invoice(
                Id.random(),
                billingProfileId,
                now,
                now.plusDays(DUE_DAY_COUNT_AFTER_CREATION),
                Number.of(sequenceNumber, companyInfo.name),
                Status.DRAFT,
                companyInfo.vatRegulationState() == Invoice.VatRegulationState.APPLICABLE ? BigDecimal.valueOf(0.2) : BigDecimal.ZERO
        ).companyInfo(companyInfo);
    }

    public enum Status {
        DRAFT, TO_REVIEW, REJECTED, APPROVED, PAID
    }

    public BillingProfile.Type billingProfileType() {
        return personalInfo != null ? BillingProfile.Type.INDIVIDUAL : BillingProfile.Type.COMPANY;
    }

    public Money totalBeforeTax() {
        return rewards.stream().map(Invoice.Reward::base).reduce(Money::add)
                .orElseThrow(() -> notFound("No reward found for invoice %s".formatted(number())));
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

    public String externalFileName() {
        return "%s.pdf".formatted(number);
    }

    public String internalFileName() {
        return "%s.pdf".formatted(id);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    @Getter
    @Accessors(fluent = true)
    public static class Number {
        private final @NonNull String value;

        public static Number of(Integer sequenceNumber, String... parts) {
            return new Number("OD-%s-%03d".formatted(normalize(parts), sequenceNumber));
        }

        public String toString() {
            return value;
        }

        public static Number fromString(final @NonNull String value) {
            return new Number(value);
        }

        private static String normalize(String... parts) {
            return Stream.of(parts).map(Number::normalize).collect(Collectors.joining("-"));
        }

        private static String normalize(final @NonNull String part) {
            return StringUtils.stripAccents(part)
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toUpperCase();
        }
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
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

    // TODO, store the vatRegulationState
    public record CompanyInfo(@NonNull String registrationNumber,
                              @NonNull String name,
                              @NonNull String address,
                              @NonNull Boolean subjectToEuVAT,
                              @NonNull Boolean inEuropeanUnion,
                              @NonNull Boolean isFrance,
                              String euVATNumber
    ) {
        public VatRegulationState vatRegulationState() {
            if (!inEuropeanUnion) return VatRegulationState.NOT_APPLICABLE_NON_UE;
            if (!isFrance) return VatRegulationState.REVERSE_CHARGE;
            if (!subjectToEuVAT) return VatRegulationState.NOT_APPLICABLE_FRENCH_NOT_SUBJECT;
            return VatRegulationState.APPLICABLE;
        }
    }

    public record Reward(@NonNull RewardId id, @NonNull ZonedDateTime createdAt, @NonNull String projectName,
                         @NonNull Money amount, @NonNull Money base, Invoice.Id invoiceId) {
    }

    public enum Sort {
        NUMBER, CREATED_AT, AMOUNT, STATUS
    }
}
