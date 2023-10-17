package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationJsonEntity {
    String city;
    @JsonProperty("post_code")
    String postCode;
    String address;
    String country;
}
