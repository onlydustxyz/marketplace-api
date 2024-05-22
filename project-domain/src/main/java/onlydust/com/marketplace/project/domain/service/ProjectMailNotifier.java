package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.event.NewCommitteeApplication;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.util.UUID;

@AllArgsConstructor
public class ProjectMailNotifier implements CommitteeObserverPort {

    private final OutboxPort projectMailOutboxPort;
    private final ProjectStoragePort projectStoragePort;
    private final UserStoragePort userStoragePort;
    private final CommitteeStoragePort committeeStoragePort;

    @Override
    public void onNewApplication(Committee.@NonNull Id committeeId, @NonNull UUID projectId, @NonNull UUID userId) {
        final CommitteeView committee = committeeStoragePort.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.internalServerError("Committee %s not found".formatted(committeeId.value())));
        final User user = userStoragePort.getUserById(userId).orElseThrow(() -> OnlyDustException.internalServerError("User %s not found".formatted(userId)));
        final CommitteeApplicationView.ProjectInfosView projectInfos = projectStoragePort.getProjectInfos(projectId);
        projectMailOutboxPort.push(new NewCommitteeApplication(projectInfos.name(), projectId, user.getGithubEmail(), user.getGithubLogin(), userId,
                committee.name(), committeeId.value()));
    }
}
