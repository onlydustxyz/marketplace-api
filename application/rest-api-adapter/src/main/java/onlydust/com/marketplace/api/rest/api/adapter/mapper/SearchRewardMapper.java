package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileAdminView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapInvoiceInternalStatus;

public interface SearchRewardMapper {

    static SearchRewardsResponse searchRewardToResponse(final List<RewardView> rewardViews) {
        final SearchRewardsResponse searchRewardsResponse = new SearchRewardsResponse();
        for (RewardView view : rewardViews) {
            searchRewardsResponse.addRewardsItem(mapToItem(view));
        }
        return searchRewardsResponse;
    }

    static SearchRewardItemResponse mapToItem(RewardView view) {
        return new SearchRewardItemResponse()
                .id(view.id())
                .githubUrls(view.githubUrls())
                .processedAt(view.processedAt())
                .requestedAt(view.requestedAt())
                .money(moneyViewToResponse(view.money())
                )
                .project(new ProjectLinkResponse()
                        .name(view.projectName())
                        .logoUrl(view.projectLogoUrl()))
                .sponsors(view.sponsors().stream()
                        .map(shortSponsorView -> new SponsorLinkResponse()
                                .name(shortSponsorView.name())
                                .avatarUrl(shortSponsorView.logoUrl()))
                        .toList())
                .billingProfile(mapBillingProfile(view.billingProfileAdmin()));
    }

    static MoneyLinkResponse moneyViewToResponse(final MoneyView view) {
        if (view == null) {
            return null;
        }
        return new MoneyLinkResponse()
                .amount(view.amount())
                .currencyCode(view.currencyCode())
                .currencyName(view.currencyName())
                .currencyLogoUrl(view.currencyLogoUrl())
                .dollarsEquivalent(view.dollarsEquivalent());
    }

    static RewardPageResponse rewardPageToResponse(int pageIndex, Page<RewardDetailsView> page) {
        final RewardPageResponse response = new RewardPageResponse();
        response.setTotalPageNumber(page.getTotalPageNumber());
        response.setTotalItemNumber(page.getTotalItemNumber());
        response.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        response.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        page.getContent().forEach(rewardDetailsView -> response.addRewardsItem(new RewardPageItemResponse()
                .id(rewardDetailsView.id().value())
                .status(RewardStatus.valueOf(rewardDetailsView.status().name()))
                .requestedAt(rewardDetailsView.requestedAt())
                .processedAt(rewardDetailsView.processedAt())
                .githubUrls(rewardDetailsView.githubUrls())
                .paidNotificationDate(rewardDetailsView.paidNotificationSentAt())
                .project(new ProjectLinkResponse()
                        .name(rewardDetailsView.project().name())
                        .logoUrl(rewardDetailsView.project().logoUrl()))
                .sponsors(rewardDetailsView.sponsors().stream()
                        .map(sponsor -> new SponsorLinkResponse()
                                .name(sponsor.name())
                                .avatarUrl(sponsor.logoUrl()))
                        .sorted(comparing(SponsorLinkResponse::getName))
                        .toList())
                .money(moneyViewToResponse(rewardDetailsView.money()))
                .billingProfile(mapBillingProfile(rewardDetailsView.billingProfileAdmin()))
                .invoice(rewardDetailsView.invoice() != null ?
                        new InvoiceLinkResponse()
                                .id(rewardDetailsView.invoice().id().value())
                                .number(rewardDetailsView.invoice().number().toString())
                                .status(mapInvoiceInternalStatus(rewardDetailsView.invoice().status()))
                        : null
                )
                .transactionHash(rewardDetailsView.transactionHash())
                .paidTo(rewardDetailsView.paidTo())
                .recipient(rewardDetailsView.recipient() != null ?
                        new RecipientLinkResponse()
                                .login(rewardDetailsView.recipient().login())
                                .avatarUrl(rewardDetailsView.recipient().avatarUrl())
                        : null)
        ));
        return response;
    }

    private static BillingProfileResponse mapBillingProfile(ShortBillingProfileAdminView billingProfileAdminView) {
        if (billingProfileAdminView == null) {
            return null;
        }
        return new BillingProfileResponse()
                .id(billingProfileAdminView.billingProfileId().value())
                .type(switch (billingProfileAdminView.billingProfileType()) {
                    case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                    case COMPANY, SELF_EMPLOYED -> BillingProfileType.COMPANY;
                })
                .kyc(billingProfileAdminView.billingProfileType().equals(BillingProfile.Type.INDIVIDUAL) ?
                        BackOfficeMapper.mapShortBillingProfileAdminToKyc(billingProfileAdminView) : null)
                .kyb(billingProfileAdminView.billingProfileType().equals(BillingProfile.Type.COMPANY) ?
                        BackOfficeMapper.mapShortBillingProfileAdminToKyb(billingProfileAdminView) : null)
                .name(billingProfileAdminView.billingProfileName())
                .verificationStatus(isNull(billingProfileAdminView.verificationStatus()) ? null :
                        switch (billingProfileAdminView.verificationStatus()) {
                            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
                            case STARTED -> VerificationStatus.STARTED;
                            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
                            case VERIFIED -> VerificationStatus.VERIFIED;
                            case REJECTED -> VerificationStatus.REJECTED;
                            case CLOSED -> VerificationStatus.CLOSED;
                        })
                .admins(List.of(new BillingProfileAdminResponse()
                        .name(billingProfileAdminView.adminName())
                        .email(billingProfileAdminView.adminEmail())
                        .login(billingProfileAdminView.adminGithubLogin())
                        .avatarUrl(billingProfileAdminView.adminGithubAvatarUrl()))
                );
    }
}
