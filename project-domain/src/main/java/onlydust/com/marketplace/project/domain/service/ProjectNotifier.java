package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectInfosView;

import java.util.UUID;

@AllArgsConstructor
public class ProjectNotifier implements CommitteeObserverPort {

    private final NotificationPort notificationPort;
    private final ProjectStoragePort projectStoragePort;
    private final CommitteeStoragePort committeeStoragePort;

    @Override
    public void onNewApplication(Committee.@NonNull Id committeeId, @NonNull UUID projectId, @NonNull UUID userId) {
        final var committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.internalServerError("Committee %s not found".formatted(committeeId.value())));
        final ProjectInfosView projectInfos = projectStoragePort.getProjectInfos(projectId);
        notificationPort.push(userId, new CommitteeApplicationCreated(projectInfos.name(), projectId,
                committee.name(), committeeId.value(), committee.applicationEndDate()));
    }
}
