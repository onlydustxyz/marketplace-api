package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Project {
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String moreInfoUrl;
    Boolean hiring;
    ProjectVisibility visibility;
    List<Tag> tags;
    Boolean botNotifyExternalApplications;

    public enum Tag {
        HOT_COMMUNITY,
        NEWBIES_WELCOME,
        LIKELY_TO_REWARD,
        WORK_IN_PROGRESS,
        FAST_AND_FURIOUS,
        BIG_WHALE,
        UPDATED_ROADMAP,
        HAS_GOOD_FIRST_ISSUES,
    }

    public static String slugOf(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]+", "")
                .replaceAll("\s+", "-")
                .toLowerCase();
    }
}
