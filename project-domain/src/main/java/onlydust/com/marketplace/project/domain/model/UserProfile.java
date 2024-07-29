package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@ToString
@Accessors(fluent = true, chain = true)
public class UserProfile {
    private String avatarUrl;
    private String location;
    private String bio;
    private String website;
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();
    private UserAllocatedTimeToContribute allocatedTimeToContribute;
    private Boolean isLookingForAJob;
    private String firstName;
    private String lastName;
}
