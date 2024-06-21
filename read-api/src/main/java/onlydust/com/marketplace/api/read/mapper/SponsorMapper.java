package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.backoffice.api.contract.model.SponsorLinkResponse;
import onlydust.com.marketplace.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectSponsorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.SponsorViewEntity;
import onlydust.com.marketplace.kernel.mapper.DateMapper;

import java.time.ZonedDateTime;

import static java.util.Objects.isNull;

public interface SponsorMapper {
    static final int MONTHS_SINCE_LAST_ALLOCATION_TO_BE_A_SPONSOR = 6;

    static SponsorLinkResponse mapNullableBO(final SponsorViewEntity entity) {
        return isNull(entity) ? null : new SponsorLinkResponse()
                .id(entity.getId())
                .name(entity.getName())
                .avatarUrl(entity.getLogoUrl());
    }

    static SponsorResponse mapNullabled(final SponsorViewEntity entity) {
        return isNull(entity) ? null : new SponsorResponse()
                .id(entity.getId())
                .name(entity.getName())
                .logoUrl(entity.getLogoUrl())
                .url(entity.getUrl());
    }

    static boolean isActive(ProjectSponsorViewEntity projectSponsor) {
        return projectSponsor.lastAllocationDate() == null ||
                DateMapper.ofNullable(projectSponsor.lastAllocationDate()).isAfter(ZonedDateTime.now().minusMonths(MONTHS_SINCE_LAST_ALLOCATION_TO_BE_A_SPONSOR));
    }
}
