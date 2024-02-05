package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.Reward;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.List;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort {

    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;


    @Override
    public void createReward(Reward reward) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
    }

    @Override
    public List<Project> listProjectsByRecipient(Long githubUserId) {
        return shortProjectViewEntityRepository.listProjectsByRewardRecipient(githubUserId)
                .stream()
                .map(ProjectMapper::mapShortProjectViewToProject)
                .toList();
    }
}
