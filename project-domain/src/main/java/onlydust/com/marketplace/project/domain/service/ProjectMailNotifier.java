package onlydust.com.marketplace.project.domain.service;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;

import java.util.UUID;

public class ProjectMailNotifier implements CommitteeObserverPort {

    private final OutboxPort projectMailOutboxPort;

    @Override
    public void onNewApplication(Committee.@NonNull Id committeeId, @NonNull UUID projectId, @NonNull UUID userId) {

    }
}
