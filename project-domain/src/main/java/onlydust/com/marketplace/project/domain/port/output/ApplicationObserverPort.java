package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Application;

public interface ApplicationObserverPort {
    void onApplicationCreated(Application application);

    void onApplicationAccepted(Application application);
}
