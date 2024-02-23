package onlydust.com.marketplace.project.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class OldAccountNumber {
    private final String value;

    public String asString() {
        return value;
    }
}
