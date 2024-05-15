package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.TechnologyQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.TechnologyViewEntityRepository;

import java.util.List;

@AllArgsConstructor
public class PostgresTechnologyAdapter implements TechnologyStoragePort {

    private final TechnologyViewEntityRepository technologyViewEntityRepository;

    @Override
    public List<String> getAllUsedTechnologies() {
        return technologyViewEntityRepository.findAcrossAllProjects().stream()
                .map(TechnologyQueryEntity::getTechnology).toList();
    }
}
