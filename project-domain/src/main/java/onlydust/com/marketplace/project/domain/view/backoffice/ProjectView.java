package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class ProjectView {
    UUID id;
    String name;
    String shortDescription;
    String longDescription;
    List<String> moreInfoLinks;
    String logoUrl;
    Boolean hiring;
    Integer rank;
    ProjectVisibility visibility;
    List<UUID> projectLeadIds;
    ZonedDateTime createdAt;
    Long activeContributors;
    Long newContributors;
    Long uniqueRewardedContributors;
    Long openedIssues;
    Long contributions;
    BigDecimal dollarsEquivalentAmountSent;
    BigDecimal strkAmountSent;
}
