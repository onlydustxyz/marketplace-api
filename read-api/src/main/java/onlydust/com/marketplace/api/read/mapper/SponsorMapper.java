package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.backoffice.api.contract.model.SponsorLinkResponse;
import onlydust.com.marketplace.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;

import static java.util.Objects.isNull;

public interface SponsorMapper {
    static SponsorLinkResponse mapNullableBO(final SponsorReadEntity entity) {
        return isNull(entity) ? null : new SponsorLinkResponse()
                .id(entity.id())
                .name(entity.name())
                .avatarUrl(entity.logoUrl());
    }

    static SponsorResponse mapNullabled(final SponsorReadEntity entity) {
        return isNull(entity) ? null : new SponsorResponse()
                .id(entity.id())
                .name(entity.name())
                .logoUrl(entity.logoUrl())
                .url(entity.url());
    }
}
