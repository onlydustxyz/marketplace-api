package onlydust.com.marketplace.kernel.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@SuperBuilder
public class Page<T> {
    private final List<T> content;
    private final int totalPageNumber;
    private final int totalItemNumber;
    @Builder.Default
    private final Map<String, Set<Object>> filters = new HashMap<>();
}
