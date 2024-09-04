package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;

public interface ProjectServicePort {
    void onFundsAllocatedToProgram(@NonNull SponsorId sponsorId, @NonNull ProgramId programId, @NonNull PositiveAmount amount, @NonNull Currency.Id currencyId);
}
