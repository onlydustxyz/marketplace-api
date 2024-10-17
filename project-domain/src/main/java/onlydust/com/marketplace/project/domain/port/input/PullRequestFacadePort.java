package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.UpdatePullRequestCommand;

public interface PullRequestFacadePort {

    void updatePullRequest(UserId projectLeadId, UpdatePullRequestCommand updatePullRequestCommand);
}
