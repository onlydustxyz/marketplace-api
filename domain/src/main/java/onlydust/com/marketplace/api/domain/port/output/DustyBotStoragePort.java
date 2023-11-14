package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.api.domain.view.CreatedAndClosedIssueView;

public interface DustyBotStoragePort {

    CreatedAndClosedIssueView createIssue(final CreateAndCloseIssueCommand createAndCloseIssueCommand);

    CreatedAndClosedIssueView closeIssue(final CreateAndCloseIssueCommand createAndCloseIssueCommand);
}
