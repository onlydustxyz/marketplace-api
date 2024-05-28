package onlydust.com.marketplace.bff.read.mapper;

import onlydust.com.backoffice.api.contract.model.SponsorLinkResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.SponsorViewEntity;

import static java.util.Objects.isNull;

public interface SponsorMapper {
    static SponsorLinkResponse mapNullable(final SponsorViewEntity entity) {
        return isNull(entity) ? null : new SponsorLinkResponse()
                .id(entity.getId())
                .name(entity.getName())
                .avatarUrl(entity.getLogoUrl());
    }
}
