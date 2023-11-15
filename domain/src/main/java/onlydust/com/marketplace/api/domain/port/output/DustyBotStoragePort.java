package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.RewardableItemView;

public interface DustyBotStoragePort {

    RewardableItemView createIssue(final String repoOwner,
                                   final String repoName,
                                   final String title,
                                   final String description);

    RewardableItemView closeIssue(final String repoOwner,
                                  final String repoName,
                                  final Long issueNumber);
}
