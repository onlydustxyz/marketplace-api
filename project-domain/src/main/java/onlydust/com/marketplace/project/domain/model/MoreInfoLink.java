package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoreInfoLink {
    String url;
    String value;
}
