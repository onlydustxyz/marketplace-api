package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiWorldMapItemResponse;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@Immutable
public class WorldMapKpiReadEntity {
    @Id
    String countryCode;

    BigDecimal value;

    public BiWorldMapItemResponse toListItemResponse() {
        return new BiWorldMapItemResponse()
                .countryCode(countryCode)
                .value(value);
    }
}