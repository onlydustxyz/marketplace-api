package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonJsonEntity {
    @JsonProperty("Person")
    Value value;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        @JsonProperty("firstname")
        String firstName;
        @JsonProperty("lastname")
        String lastName;
    }
}
