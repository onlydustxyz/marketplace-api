package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface ProgramObserverPort {
    void onFundsAllocatedToProgram(@NonNull SponsorId sponsorId, @NonNull ProgramId programId, @NonNull BigDecimal amount, @NonNull UUID currencyId);

    void onFundsRefundedByProgram(@NonNull ProgramId programId, @NonNull SponsorId sponsorId, @NonNull BigDecimal amount, @NonNull UUID currencyId);

    void onFundsRefundedByProject(@NonNull ProgramId programId, @NonNull ProjectId projectId, @NonNull BigDecimal amount, @NonNull UUID currencyId);

    void onDepositRejected(@NonNull UUID depositId, @NonNull SponsorId sponsorId, @NonNull BigDecimal amount, @NonNull UUID currencyId,
                           @NonNull ZonedDateTime timestamp);

    void onDepositApproved(@NonNull UUID depositId, @NonNull SponsorId sponsorId, @NonNull BigDecimal amount, @NonNull UUID currencyId,
                           @NonNull ZonedDateTime timestamp);
}
