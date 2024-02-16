package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
@Accessors(fluent = true)
public class Sponsor {
    UUID id;
    String name;
    String url;
    String logoUrl;
}
