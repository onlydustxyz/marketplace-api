package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Page<T> {

    List<T> content;
    int totalPageNumber;
    int totalItemNumber;
}
