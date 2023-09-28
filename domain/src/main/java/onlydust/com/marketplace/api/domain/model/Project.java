package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class Project {
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String moreInfoUrl;
    Boolean hiring;
}
