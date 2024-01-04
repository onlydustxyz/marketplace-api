package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;

public interface SponsorMapper {

  static SponsorView mapToSponsorView(SponsorEntity sponsor) {
    return SponsorView.builder()
        .id(sponsor.getId())
        .name(sponsor.getName())
        .url(sponsor.getUrl())
        .logoUrl(sponsor.getLogoUrl())
        .build();
  }
}
