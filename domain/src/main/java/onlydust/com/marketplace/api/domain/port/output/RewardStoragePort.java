package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Project;

import java.util.List;

public interface RewardStoragePort {
    List<Project> listProjectsByRecipient(Long githubUserId);
}
