package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class ProjectView {
    UUID id;
    String name;
    String logoUrl;
}
