package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.model.notification.FundsAllocatedToProgram;
import onlydust.com.marketplace.project.domain.model.notification.FundsUnallocatedFromProgram;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProgramObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.SponsorStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectInfosView;

import java.math.BigDecimal;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class ProjectNotifier implements CommitteeObserverPort, ProgramObserverPort {

    private final NotificationPort notificationPort;
    private final ProjectStoragePort projectStoragePort;
    private final CommitteeStoragePort committeeStoragePort;
    private final ProgramStoragePort programStoragePort;
    private final SponsorStoragePort sponsorStoragePort;

    @Override
    public void onNewApplication(Committee.@NonNull Id committeeId, @NonNull UUID projectId, @NonNull UUID userId) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> internalServerError("Committee %s not found".formatted(committeeId.value())));
        final ProjectInfosView projectInfos = projectStoragePort.getProjectInfos(projectId);
        notificationPort.push(userId, new CommitteeApplicationCreated(projectInfos.name(), projectId,
                committee.name(), committeeId.value(), committee.applicationEndDate()));
    }

    @Override
    public void onFundsAllocatedToProgram(@NonNull SponsorId sponsorId, @NonNull ProgramId programId, @NonNull BigDecimal amount, @NonNull UUID currencyId) {
        final var program = programStoragePort.findById(programId)
                .orElseThrow(() -> internalServerError("Program %s not found".formatted(programId.value())));

        program.leadIds().forEach(leadId -> notificationPort.push(leadId.value(),
                FundsAllocatedToProgram.builder()
                        .sponsorId(sponsorId)
                        .programId(programId)
                        .amount(amount)
                        .currencyId(currencyId)
                        .build()));
    }

    @Override
    public void onFundsRefundedByProgram(@NonNull ProgramId programId, @NonNull SponsorId sponsorId, @NonNull BigDecimal value, UUID value1) {
        final var sponsorLeads = sponsorStoragePort.findSponsorLeads(sponsorId);

        sponsorLeads.forEach(leadId -> notificationPort.push(leadId.value(),
                FundsUnallocatedFromProgram.builder()
                        .sponsorId(sponsorId)
                        .programId(programId)
                        .amount(value)
                        .currencyId(value1)
                        .build()));
    }
}
