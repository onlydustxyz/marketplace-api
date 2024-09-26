package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Application;

public interface ApplicationObserverPort {
    void onApplicationCreated(Application application);

    void onApplicationAccepted(Application application, UserId projectLeadId);

    void onApplicationRefused(Application application);
}
