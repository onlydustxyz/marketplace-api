package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class CommitteeService implements CommitteeFacadePort {

    private final CommitteeStoragePort committeeStoragePort;
    private final PermissionService permissionService;
    private final ProjectStoragePort projectStoragePort;

    @Override
    public Committee createCommittee(@NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        return committeeStoragePort.save(new Committee(name, startDate, endDate));
    }

    @Override
    public Page<CommitteeLinkView> getCommittees(Integer pageIndex, Integer pageSize) {
        return committeeStoragePort.findAll(pageIndex, pageSize);
    }

    @Override
    public void update(Committee committee) {
        committeeStoragePort.save(committee);
    }

    @Override
    public CommitteeView getCommitteeById(Committee.Id committeeId) {
        return committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));
    }

    @Override
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeStoragePort.updateStatus(committeeId, status);
    }

    @Override
    public void createUpdateApplicationForCommittee(Committee.Id committeeId, Committee.Application application) {
        final CommitteeView committeeView = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s was not found".formatted(committeeId.value().toString())));
        if (!permissionService.isUserProjectLead(application.projectId(), application.userId()))
            throw OnlyDustException.forbidden("Only project leads send new application to committee");
        if (!projectStoragePort.exists(application.projectId()))
            throw OnlyDustException.internalServerError("Project %s was not found".formatted(application.projectId()));
        if (committeeView.status() != Committee.Status.OPEN_TO_APPLICATIONS)
            throw OnlyDustException.forbidden("Applications are not opened or are closed for committee %s".formatted(committeeId.value()));
        committeeStoragePort.saveApplication(committeeId, application);
    }
}
