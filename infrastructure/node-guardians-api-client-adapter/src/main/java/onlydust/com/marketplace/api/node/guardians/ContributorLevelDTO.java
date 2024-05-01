package onlydust.com.marketplace.api.node.guardians;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContributorLevelDTO {
    @JsonProperty("level")
    Integer level;
}
