package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Committee;

public interface CommitteeStoragePort {
    Committee save(Committee committee);
}
