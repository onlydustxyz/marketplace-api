package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ProjectRepositoryView {
    Long id;
    String name;
    String owner;
    UUID projectId;
    Map<String,Long> technologies;
}
