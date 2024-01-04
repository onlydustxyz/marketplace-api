package onlydust.com.marketplace.api.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

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
  ProjectVisibility visibility;
}
