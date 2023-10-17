package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyJsonEntity {
    String name;
    @JsonProperty("identification_number")
    String identificationNumber;
    PersonJsonEntity owner;
}
