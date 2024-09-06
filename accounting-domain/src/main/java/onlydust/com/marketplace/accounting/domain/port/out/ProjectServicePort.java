package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface ProjectServicePort {
    void onFundsAllocatedToProgram(@NonNull SponsorId sponsorId, @NonNull ProgramId programId, @NonNull PositiveAmount amount, @NonNull Currency.Id currencyId);

    void onFundsRefundedByProgram(@NonNull ProgramId programId, @NonNull SponsorId sponsorId, @NonNull PositiveAmount amount, @NonNull Currency.Id currencyId);

    void onDepositRejected(@NonNull Deposit.Id id, @NonNull SponsorId sponsorId, BigDecimal amount, Currency.Id currencyId, ZonedDateTime timestamp);

    void onDepositApproved(@NonNull Deposit.Id id, @NonNull SponsorId sponsorId, BigDecimal amount, Currency.Id currencyId, ZonedDateTime timestamp);
}
