package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;

import java.util.List;

@AllArgsConstructor
public class TechnologiesService implements TechnologiesPort {
    private final TechnologyStoragePort technologyStoragePort;

    @Override
    public List<String> getAllUsedTechnologies() {
        return technologyStoragePort.getAllUsedTechnologies();
    }
}
