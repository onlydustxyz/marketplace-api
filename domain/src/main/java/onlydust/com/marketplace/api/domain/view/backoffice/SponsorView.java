package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class SponsorView {
    UUID id;
    String name;
    String url;
    String logoUrl;
    List<UUID> projectIds;
}
