package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;

public interface DustyBotStoragePort {

    RewardableItemView createIssue(final GithubRepo repo,
                                   final String title,
                                   final String description);

    RewardableItemView closeIssue(final GithubRepo repo,
                                  final Long issueNumber);
}
