package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@ToString
public class UserProfile {
    private String avatarUrl;
    private String location;
    private String bio;
    private String website;
    @Builder.Default
    private Map<String, Long> technologies = new HashMap<>();
    private UserProfileCover cover;
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();
    private UserAllocatedTimeToContribute allocatedTimeToContribute;
    private Boolean isLookingForAJob;
    private String firstName;
    private String lastName;
}
