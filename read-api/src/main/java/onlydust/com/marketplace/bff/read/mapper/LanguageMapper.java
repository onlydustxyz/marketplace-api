package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.LanguageResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.LanguageViewEntity;

public interface LanguageMapper {
    static LanguageResponse map(final @NonNull LanguageViewEntity language) {
        return new LanguageResponse()
                .id(language.id())
                .name(language.name())
                .logoUrl(language.logoUrl())
                .bannerUrl(language.bannerUrl());
    }
}
