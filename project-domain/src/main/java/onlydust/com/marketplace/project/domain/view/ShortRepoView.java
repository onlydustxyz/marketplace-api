package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortRepoView {
    Long id;
    String owner;
    String name;
    String description;
    String htmlUrl;
}
