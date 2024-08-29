package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.util.UUID;

public record Deposit(@NonNull Id id,
                      @NonNull SponsorId sponsorId,
                      @NonNull Blockchain.TransferTransaction transaction,
                      @NonNull Currency currency,
                      @NonNull Status status,
                      BillingInformation billingInformation) {

    public static Deposit preview(final @NonNull SponsorId sponsorId,
                                  final @NonNull Blockchain.TransferTransaction transaction,
                                  final @NonNull Currency currency,
                                  final BillingInformation billingInformation) {
        return new Deposit(Id.random(), sponsorId, transaction, currency, Status.DRAFT, billingInformation);
    }

    public enum Status {
        DRAFT, PENDING, COMPLETED, REJECTED
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

    public record BillingInformation(String companyName,
                                     String companyAddress,
                                     String companyCountry,
                                     String companyId,
                                     String vatNumber,
                                     String billingEmail,
                                     String firstName,
                                     String lastName,
                                     String email) {
    }

}
