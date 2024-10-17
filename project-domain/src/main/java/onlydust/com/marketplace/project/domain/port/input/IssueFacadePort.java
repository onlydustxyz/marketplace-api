package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.UpdateIssueCommand;

public interface IssueFacadePort {

    void updateIssue(UserId projectLeadId, UpdateIssueCommand updateIssueCommand);
}
