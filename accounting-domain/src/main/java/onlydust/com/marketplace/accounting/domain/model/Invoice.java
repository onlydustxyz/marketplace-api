package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;

import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Data
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Invoice {
    public static final int DUE_DAY_COUNT_AFTER_CREATION = 10;

    private final @NonNull Id id;
    private final @NonNull BillingProfileSnapshot billingProfileSnapshot;
    private final @NonNull UserId createdBy;
    private final @NonNull ZonedDateTime createdAt;
    private final @NonNull ZonedDateTime dueAt;
    private final @NonNull Invoice.Number number;
    private @NonNull Status status;
    private @NonNull List<Reward> rewards = new ArrayList<>();
    private URL url;
    private String originalFileName;
    private String rejectionReason;

    public static Invoice of(final @NonNull BillingProfileView billingProfile, int sequenceNumber, final @NonNull UserId createdBy) {
        if (billingProfile.getPayoutInfo() == null) {
            throw internalServerError("An invoice can only be created on a billing profile with payout info (billing profile %s)".formatted(billingProfile.getId()));
        }
        final var now = ZonedDateTime.now();
        return new Invoice(
                Id.random(),
                BillingProfileSnapshot.of(billingProfile, billingProfile.getPayoutInfo()),
                createdBy,
                now,
                now.plusDays(DUE_DAY_COUNT_AFTER_CREATION),
                isNull(billingProfile.getKyc()) ?
                        Number.of(sequenceNumber, billingProfile.getKyb().getName()) :
                        Number.of(sequenceNumber, billingProfile.getKyc().getLastName(), billingProfile.getKyc().getFirstName()),
                Status.DRAFT
        );
    }

    public Collection<TotalMoneyView> totals() {
        return rewards.stream()
                .collect(groupingBy(r -> r.amount.currency,
                        mapping(r -> new TotalMoneyView(r.amount.value, r.amount.currency.toView(), r.target.value),
                                reducing(null, TotalMoneyView::add))))
                .values().stream()
                .sorted(Comparator.comparing(r -> r.currency().code().toString()))
                .toList();
    }

    public enum Status {
        DRAFT, TO_REVIEW, REJECTED, APPROVED, PAID;

        public boolean isActive() {
            return this != DRAFT && this != REJECTED;
        }
    }

    public BillingProfile.Type billingProfileType() {
        return billingProfileSnapshot.type();
    }

    public Money totalBeforeTax() {
        return rewards.stream().map(Invoice.Reward::target).reduce(Money::add)
                .orElseThrow(() -> notFound("No reward found for invoice %s".formatted(number())));
    }

    public BigDecimal taxRate() {
        return billingProfileSnapshot.kyb().map(BillingProfileSnapshot.KybSnapshot::taxRate).orElse(BigDecimal.ZERO);
    }

    public Money totalTax() {
        return totalBeforeTax().multiply(taxRate());
    }

    public Money totalAfterTax() {
        return totalBeforeTax().add(totalTax());
    }

    public Optional<BankAccount> bankAccount() {
        return Optional.ofNullable(billingProfileSnapshot.bankAccount()).filter(b -> networks().contains(Network.SEPA));
    }

    public List<Wallet> wallets() {
        return billingProfileSnapshot.wallets().stream().filter(w -> networks().contains(w.network())).toList();
    }

    private List<Network> networks() {
        return rewards.stream().flatMap(r -> r.networks().stream()).distinct().toList();
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
            return Stream.of(parts).filter(Objects::nonNull).map(Number::normalize).collect(Collectors.joining("-"));
        }

        private static String normalize(final @NonNull String part) {
            return part
                    .replaceAll("\\p{P}", "-")
                    .replaceAll("\\s", "-")
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


    public record BillingProfileSnapshot(
            @NonNull BillingProfile.Id id,
            @NonNull BillingProfile.Type type,
            KycSnapshot kycSnapshot,
            KybSnapshot kybSnapshot,
            BankAccount bankAccount,
            @NonNull List<Wallet> wallets) {

        public static BillingProfileSnapshot of(final @NonNull BillingProfileView billingProfile, final @NonNull PayoutInfo payoutInfo) {
            return new BillingProfileSnapshot(
                    billingProfile.getId(),
                    billingProfile.getType(),
                    isNull(billingProfile.getKyc()) ? null : KycSnapshot.of(billingProfile.getKyc()),
                    isNull(billingProfile.getKyb()) ? null : KybSnapshot.of(billingProfile.getKyb()),
                    payoutInfo.getBankAccount(),
                    payoutInfo.wallets()
            );
        }

        public String subject() {
            return switch (type) {
                case INDIVIDUAL -> kyc().map(KycSnapshot::fullName).orElseThrow(() -> internalServerError("No KYC found for individual billing profile"));
                case SELF_EMPLOYED, COMPANY ->
                        kyb().map(KybSnapshot::name).orElseThrow(() -> internalServerError("No KYB found for company/self-employed billing profile"));
            };
        }

        public Optional<KycSnapshot> kyc() {
            return Optional.ofNullable(kycSnapshot);
        }

        public Optional<KybSnapshot> kyb() {
            return Optional.ofNullable(kybSnapshot);
        }

        public Optional<Wallet> wallet(Network network) {
            return switch (network) {
                case ETHEREUM, OPTIMISM, STARKNET, APTOS -> wallets.stream().filter(w -> w.network() == network).findFirst();
                case SEPA -> Optional.ofNullable(bankAccount).map(b -> new Wallet(network, b.accountNumber()));
            };
        }

        public record KycSnapshot(@NonNull String firstName, String lastName, @NonNull String address, @NonNull String countryCode,
                                  @NonNull Boolean usCitizen) {
            public static KycSnapshot of(Kyc kyc) {
                return new KycSnapshot(kyc.getFirstName(), kyc.getLastName(), kyc.getAddress(), kyc.getCountry().iso3Code(), kyc.getUsCitizen());
            }

            @Deprecated
            public String countryName() {
                return Country.fromIso3(countryCode).display().orElse(countryCode);
            }

            public String fullName() {
                return lastName == null ? firstName : "%s %s".formatted(firstName, lastName);
            }
        }

        public record KybSnapshot(@NonNull String registrationNumber,
                                  @NonNull String name,
                                  @NonNull String address,
                                  @NonNull String countryCode,
                                  @NonNull Boolean usEntity,
                                  @NonNull Boolean subjectToEuVAT,
                                  @NonNull Boolean inEuropeanUnion,
                                  @NonNull Boolean isFrance,
                                  @NonNull VatRegulationState vatRegulationState,
                                  @NonNull BigDecimal taxRate,
                                  String euVATNumber
        ) {
            public static KybSnapshot of(Kyb kyb) {
                final var vatRegulationState = vatRegulationStateOf(kyb);
                return new KybSnapshot(
                        kyb.getRegistrationNumber(),
                        kyb.getName(),
                        kyb.getAddress(),
                        kyb.getCountry().iso3Code(),
                        kyb.getUsEntity(),
                        kyb.getSubjectToEuropeVAT(),
                        kyb.getCountry().inEuropeanUnion(),
                        kyb.getCountry().isFrance(),
                        vatRegulationState,
                        vatRegulationState == VatRegulationState.APPLICABLE ? BigDecimal.valueOf(0.2) : BigDecimal.ZERO,
                        kyb.getEuVATNumber()
                );
            }

            private static VatRegulationState vatRegulationStateOf(Kyb kyb) {
                if (!kyb.getCountry().inEuropeanUnion()) return VatRegulationState.NOT_APPLICABLE_NON_UE;
                if (!kyb.getCountry().isFrance()) return VatRegulationState.REVERSE_CHARGE;
                if (!kyb.getSubjectToEuropeVAT()) return VatRegulationState.NOT_APPLICABLE_FRENCH_NOT_SUBJECT;
                return VatRegulationState.APPLICABLE;
            }

            @Deprecated
            public String countryName() {
                return Country.fromIso3(countryCode).display().orElse(countryCode);
            }
        }
    }

    public record Reward(@NonNull RewardId id, @NonNull ZonedDateTime createdAt, @NonNull String projectName,
                         @NonNull Money amount, @NonNull Money target, Invoice.Id invoiceId, List<Network> networks) {
        public Reward withNetworks(List<Network> networks) {
            return new Reward(id, createdAt, projectName, amount, target, invoiceId, networks);
        }
    }

    public enum Sort {
        NUMBER, CREATED_AT, AMOUNT, STATUS
    }
}
