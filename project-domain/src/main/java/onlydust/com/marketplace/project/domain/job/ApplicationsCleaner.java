package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;

@Slf4j
@AllArgsConstructor
public class ApplicationsCleaner {
    private final ProjectApplicationStoragePort projectApplicationStoragePort;

    public void cleanUp() {
        projectApplicationStoragePort.deleteObsoleteApplications();
    }
}
