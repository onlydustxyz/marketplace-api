package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.backoffice.api.contract.model.SponsorLinkResponse;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;

import java.net.URI;

import static java.util.Objects.isNull;

public interface SponsorMapper {
    static SponsorLinkResponse mapNullableBO(final SponsorReadEntity entity) {
        return isNull(entity) ? null : new SponsorLinkResponse()
                .id(entity.id())
                .name(entity.name())
                .avatarUrl(entity.logoUrl());
    }

    static onlydust.com.marketplace.api.contract.model.SponsorLinkResponse mapNullabled(final SponsorReadEntity entity) {
        return isNull(entity) ? null : new onlydust.com.marketplace.api.contract.model.SponsorLinkResponse()
                .id(entity.id())
                .name(entity.name())
                .logoUrl(entity.logoUrl() == null ? null : URI.create(entity.logoUrl()));
    }
}
