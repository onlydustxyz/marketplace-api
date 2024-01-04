package onlydust.com.marketplace.api.domain.view.backoffice;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectRepositoryView {

  Long id;
  String name;
  String owner;
  UUID projectId;
  Map<String, Long> technologies;
}
