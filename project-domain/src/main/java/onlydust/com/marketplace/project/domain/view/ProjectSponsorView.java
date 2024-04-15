package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Project;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@Accessors(fluent = true)
public class ProjectSponsorView {
    private static final int MONTHS_SINCE_LAST_ALLOCATION_TO_BE_A_SPONSOR = 6;

    UUID sponsorId;
    UUID projectId;
    String sponsorLogoUrl;
    String sponsorName;
    String sponsorUrl;
    ZonedDateTime lastAllocationDate;
    Project project;

    public boolean isActive() {
        return lastAllocationDate == null || lastAllocationDate.isAfter(ZonedDateTime.now().minusMonths(MONTHS_SINCE_LAST_ALLOCATION_TO_BE_A_SPONSOR));
    }
}
