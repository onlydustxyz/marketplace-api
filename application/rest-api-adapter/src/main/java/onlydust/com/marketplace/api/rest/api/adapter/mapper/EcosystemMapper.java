package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.EcosystemPage;
import onlydust.com.marketplace.api.contract.model.EcosystemResponse;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

public interface EcosystemMapper {

    static EcosystemPage mapToResponse(final Page<Ecosystem> ecosystemPage, final int pageIndex) {
        return new EcosystemPage()
                .hasMore(PaginationHelper.hasMore(pageIndex, ecosystemPage.getTotalPageNumber()))
                .totalItemNumber(ecosystemPage.getTotalItemNumber())
                .totalPageNumber(ecosystemPage.getTotalPageNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, ecosystemPage.getTotalPageNumber()))
                .ecosystems(ecosystemPage.getContent().stream()
                        .map(ecosystem -> new EcosystemResponse()
                                .id(ecosystem.id())
                                .name(ecosystem.name())
                                .url(ecosystem.url())
                                .logoUrl(ecosystem.logoUrl())
                        ).toList());
    }
}
