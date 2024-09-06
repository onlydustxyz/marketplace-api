package onlydust.com.marketplace.api.infrastructure.project;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectServicePort;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.service.ProjectNotifier;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class ProjectServiceAdapter implements ProjectServicePort {
    private final ProjectNotifier projectNotifier;

    @Override
    public void onFundsAllocatedToProgram(@NonNull SponsorId sponsorId,
                                          @NonNull ProgramId programId,
                                          @NonNull PositiveAmount amount,
                                          @NonNull Currency.Id currencyId) {
        projectNotifier.onFundsAllocatedToProgram(sponsorId, programId, amount.getValue(), currencyId.value());
    }

    @Override
    public void onFundsRefundedByProgram(@NonNull ProgramId programId,
                                         @NonNull SponsorId sponsorId,
                                         @NonNull PositiveAmount amount,
                                         @NonNull Currency.Id currencyId) {
        projectNotifier.onFundsRefundedByProgram(programId, sponsorId, amount.getValue(), currencyId.value());
    }

    @Override
    public void onDepositRejected(@NonNull Deposit.Id id,
                                  @NonNull SponsorId sponsorId,
                                  @NonNull BigDecimal amount,
                                  @NonNull Currency.Id currencyId,
                                  @NonNull ZonedDateTime timestamp) {
        projectNotifier.onDepositRejected(id.value(), sponsorId, amount, currencyId.value(), timestamp);
    }

    @Override
    public void onDepositApproved(@NonNull Deposit.Id id,
                                  @NonNull SponsorId sponsorId,
                                  @NonNull BigDecimal amount,
                                  @NonNull Currency.Id currencyId,
                                  @NonNull ZonedDateTime timestamp) {
        projectNotifier.onDepositApproved(id.value(), sponsorId, amount, currencyId.value(), timestamp);
    }
}
