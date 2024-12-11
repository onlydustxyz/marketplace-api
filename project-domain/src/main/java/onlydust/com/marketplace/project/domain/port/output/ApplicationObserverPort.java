package onlydust.com.marketplace.project.domain.port.output;

import jakarta.transaction.Transactional;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Application;

public interface ApplicationObserverPort {
    @Transactional
    default void onApplicationCreated(Application application) {
        onApplicationCreationStarted(application);
        onApplicationCreationCompleted(application);
    }

    void onApplicationCreationStarted(Application application);
    
    void onApplicationCreationCompleted(Application application);

    void onApplicationAccepted(Application application, UserId projectLeadId);

    void onApplicationRefused(Application application);

    void onApplicationDeleted(Application application);
}
