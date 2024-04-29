package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

@Builder
@Value
@Accessors(fluent = true)
public class BillingProfileLinkView {
    UUID id;
    Role role;

    public enum Role {
        ADMIN, MEMBER;
    }
}
