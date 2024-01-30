package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;

public interface SponsorMapper {

    static SponsorView mapToDomain(final SponsorEntity sponsorEntity) {
        return SponsorView.builder()
                .id(sponsorEntity.getId())
                .url(sponsorEntity.getUrl())
                .name(sponsorEntity.getName())
                .logoUrl(sponsorEntity.getLogoUrl())
                .build();
    }
}
