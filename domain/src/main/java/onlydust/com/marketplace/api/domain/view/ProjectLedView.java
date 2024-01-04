package onlydust.com.marketplace.api.domain.view;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectLedView {

  UUID id;
  String slug;
  String name;
  String logoUrl;
  Long contributorCount;
}
