package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ShortRepoView {
    Long id;
    String owner;
    String name;
    String description;
    String htmlUrl;
}
