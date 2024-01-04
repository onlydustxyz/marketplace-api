package onlydust.com.marketplace.api.domain.view;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SponsorView {

  UUID id;
  String logoUrl;
  String name;
  String url;
}
