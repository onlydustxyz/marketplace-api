package onlydust.com.marketplace.api.postgres.adapter;

import java.util.List;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.TechnologyViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.TechnologyViewEntityRepository;

@AllArgsConstructor
public class PostgresTechnologyAdapter implements TechnologyStoragePort {

  private final TechnologyViewEntityRepository technologyViewEntityRepository;

  @Override
  public List<String> getAllUsedTechnologies() {
    return technologyViewEntityRepository.findAcrossAllProjects().stream()
        .map(TechnologyViewEntity::getTechnology).toList();
  }
}
