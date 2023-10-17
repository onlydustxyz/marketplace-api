package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PersonJsonEntity {
    @JsonProperty("firstname")
    String firstName;
    @JsonProperty("lastname")
    String lastName;
}
