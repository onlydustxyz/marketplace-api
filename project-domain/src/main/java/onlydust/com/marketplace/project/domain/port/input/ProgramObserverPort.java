package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProgramObserverPort {
    void onFundsAllocatedToProgram(@NonNull SponsorId sponsorId, @NonNull ProgramId programId, @NonNull BigDecimal amount, @NonNull UUID currencyId);

    void onFundsRefundedByProgram(@NonNull ProgramId programId, @NonNull SponsorId sponsorId, @NonNull BigDecimal value, UUID value1);
}
