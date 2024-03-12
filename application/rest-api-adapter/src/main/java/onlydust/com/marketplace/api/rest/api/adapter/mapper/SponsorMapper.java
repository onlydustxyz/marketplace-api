package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.ProjectLinkResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.backoffice.api.contract.model.SponsorPageItemResponse;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface SponsorMapper {

    static SponsorPage sponsorPageToResponse(final Page<SponsorView> sponsorViewPage, int pageIndex) {
        return new SponsorPage()
                .sponsors(sponsorViewPage.getContent().stream().map(sponsor -> new SponsorPageItemResponse()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .projects(sponsor.projects().stream().map(SponsorMapper::projectToResponse).toList())
                ).toList())
                .totalPageNumber(sponsorViewPage.getTotalPageNumber())
                .totalItemNumber(sponsorViewPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, sponsorViewPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, sponsorViewPage.getTotalPageNumber()));
    }

    static ProjectLinkResponse projectToResponse(final ShortProjectView view) {
        return new ProjectLinkResponse()
                .logoUrl(view.logoUrl())
                .name(view.name());
    }
}
