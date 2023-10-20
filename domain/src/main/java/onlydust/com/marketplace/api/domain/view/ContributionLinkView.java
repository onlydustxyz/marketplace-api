package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;

import java.util.Date;

@Value
@Builder
public class ContributionLinkView {
    String id;
    Date createdAt;
    Date completedAt;
    ContributionType type;
    ContributionStatus status;
    UserLinkView contributor;
    Long githubNumber;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    Boolean isMine;
}
