package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.contract.model.PayoutPreferencesItemResponse;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;
import onlydust.com.marketplace.api.contract.model.ShortProjectResponse;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper.mapRole;

public interface PayoutPreferenceMapper {

    static PayoutPreferencesItemResponse mapToResponse(final PayoutPreferenceView view) {
        return new PayoutPreferencesItemResponse()
                .billingProfile(isNull(view.billingProfileView()) ? null : billingProfileToShortResponse(view.billingProfileView()))
                .project(projectToResponse(view.shortProjectView()));
    }

    static ShortBillingProfileResponse billingProfileToShortResponse(final ShortBillingProfileView view) {
        return new ShortBillingProfileResponse()
                .id(view.getId().value())
                .name(view.getName())
                .enabled(view.getEnabled())
                .invoiceMandateAccepted(view.isInvoiceMandateAccepted())
                .pendingInvitationResponse(view.getPendingInvitationResponse())
                .invoiceableRewardCount(view.getInvoiceableRewardCount())
                .requestableRewardCount(view.requestableRewardCount())
                .rewardCount(view.getRewardCount())
                .missingPayoutInfo(view.getMissingPayoutInfo())
                .missingVerification(view.getMissingVerification())
                .individualLimitReached(view.getIndividualLimitReached())
                .verificationBlocked(view.isVerificationBlocked())
                .role(isNull(view.getRole()) ? null : mapRole(view.getRole()))
                .type(BillingProfileMapper.map(view.getType()));
    }


    private static ShortProjectResponse projectToResponse(final ProjectShortView view) {
        return new ShortProjectResponse()
                .id(view.id().value())
                .logoUrl(view.logoUrl())
                .shortDescription(view.shortDescription())
                .slug(view.slug())
                .name(view.name());
    }
}
