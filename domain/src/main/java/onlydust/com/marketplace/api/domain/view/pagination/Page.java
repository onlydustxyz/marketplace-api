package onlydust.com.marketplace.api.domain.view.pagination;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class Page<T> {

    List<T> content;
    int totalPageNumber;
    int totalItemNumber;
    @Builder.Default
    Map<String, Set<String>> filters = new HashMap<>();
}
