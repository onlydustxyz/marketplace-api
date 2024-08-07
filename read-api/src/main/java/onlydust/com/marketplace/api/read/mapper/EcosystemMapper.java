package onlydust.com.marketplace.api.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.EcosystemResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.EcosystemViewEntity;

public interface EcosystemMapper {
    static EcosystemResponse map(final @NonNull EcosystemViewEntity ecosystem) {
        return new EcosystemResponse()
                .id(ecosystem.id())
                .name(ecosystem.name())
                .url(ecosystem.url())
                .logoUrl(ecosystem.logoUrl())
                .slug(ecosystem.slug())
                .bannerUrl(ecosystem.bannerUrl());
    }
}
