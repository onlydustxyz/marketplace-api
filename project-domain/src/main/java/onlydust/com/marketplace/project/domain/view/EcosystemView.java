package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EcosystemView {
    UUID id;
    String logoUrl;
    String name;
    String url;
    String slug;
}
