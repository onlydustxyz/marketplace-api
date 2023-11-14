package onlydust.com.marketplace.api.domain.view;

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
}
