package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.List;

@Data
@Builder
public class CreateProjectCommand {
    String name;
    String shortDescription;
    String longDescription;
    List<Long> githubUserIdsAsProjectLeads;
    List<Long> githubRepoIds;
    List<MoreInfo> moreInfos;
    InputStream image;
    Boolean isLookingForContributors;


    @Data
    @Builder
    public static class MoreInfo {
        String url;
        String value;
    }
}
