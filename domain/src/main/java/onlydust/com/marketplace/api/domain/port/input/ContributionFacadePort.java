package onlydust.com.marketplace.api.domain.port.input;

import java.util.List;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;

public interface ContributionFacadePort {

  ContributionDetailsView getContribution(UUID projectId, String contributionId, User caller);

  void ignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds);

  void unignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds);
}
