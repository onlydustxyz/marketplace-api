package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RepositoryView {
    Integer id;
    Map<String, Integer> technologies;
}
