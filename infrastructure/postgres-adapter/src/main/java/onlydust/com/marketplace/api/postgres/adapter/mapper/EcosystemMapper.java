package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.EcosystemView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;

public interface EcosystemMapper {

    static EcosystemView mapToDomain(final EcosystemEntity ecosystemEntity) {
        return EcosystemView.builder()
                .id(ecosystemEntity.getId())
                .url(ecosystemEntity.getUrl())
                .name(ecosystemEntity.getName())
                .logoUrl(ecosystemEntity.getLogoUrl())
                .build();
    }
}
