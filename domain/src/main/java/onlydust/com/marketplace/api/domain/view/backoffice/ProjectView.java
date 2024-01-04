package onlydust.com.marketplace.api.domain.view.backoffice;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;

@Value
@Builder
@EqualsAndHashCode
public class ProjectView {

  UUID id;
  String name;
  String shortDescription;
  String longDescription;
  List<String> moreInfoLinks;
  String logoUrl;
  Boolean hiring;
  Integer rank;
  ProjectVisibility visibility;
  List<UUID> projectLeadIds;
  ZonedDateTime createdAt;
}
