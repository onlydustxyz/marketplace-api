package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectLedView {
    UUID id;
    String slug;
    String name;
    String logoUrl;
    Long contributorCount;
}
