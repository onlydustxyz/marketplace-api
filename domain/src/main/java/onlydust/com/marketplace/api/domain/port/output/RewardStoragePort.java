package onlydust.com.marketplace.api.domain.port.output;

import java.util.List;
import onlydust.com.marketplace.api.domain.model.Project;

public interface RewardStoragePort {

  List<Project> listProjectsByRecipient(Long githubUserId);
}
