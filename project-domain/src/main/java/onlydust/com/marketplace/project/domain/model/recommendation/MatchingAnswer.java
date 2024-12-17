package onlydust.com.marketplace.project.domain.model.recommendation;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class MatchingAnswer<T> {
    String body;
    boolean chosen;
    T value;

    public String valueString() {
        return value.toString();
    }
}
