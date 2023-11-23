package onlydust.com.marketplace.api.domain.view.pagination;

import lombok.Builder;
import lombok.Value;

import java.util.*;

@Value
@Builder
public class Page<T> {

    List<T> content;
    int totalPageNumber;
    int totalItemNumber;
    @Builder.Default
    Map<String, Set<Object>> filters = new HashMap<>();
}
