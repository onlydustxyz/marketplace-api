package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SponsorView {
    UUID id;
    String logoUrl;
    String name;
    String url;
}
