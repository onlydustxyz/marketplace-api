package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

@Slf4j
@AllArgsConstructor
public class ApplicationsCleaner {
    private final UserStoragePort userStoragePort;

    public void cleanUp() {
        userStoragePort.deleteObsoleteApplications();
    }
}
