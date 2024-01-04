package onlydust.com.marketplace.api.postgres.adapter;

import java.util.List;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort {

  private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;


  @Override
  public List<Project> listProjectsByRecipient(Long githubUserId) {
    return shortProjectViewEntityRepository.listProjectsByRewardRecipient(githubUserId)
        .stream()
        .map(ProjectMapper::mapShortProjectViewToProject)
        .toList();
  }
}
