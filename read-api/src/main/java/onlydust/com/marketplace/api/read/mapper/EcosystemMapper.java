package onlydust.com.marketplace.api.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.EcosystemLinkResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.EcosystemViewEntity;

public interface EcosystemMapper {
    static EcosystemLinkResponse map(final @NonNull EcosystemViewEntity ecosystem) {
        return new EcosystemLinkResponse()
                .id(ecosystem.id())
                .name(ecosystem.name())
                .url(ecosystem.url())
                .logoUrl(ecosystem.logoUrl())
                .slug(ecosystem.slug())
                .hidden(ecosystem.hidden())
                .bannerUrl(ecosystem.bannerUrl());
    }
}
