package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.api.contract.model.PayoutPreferencesItemResponse;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;
import onlydust.com.marketplace.api.contract.model.ShortProjectResponse;

import static java.util.Objects.isNull;

public interface PayoutPreferenceMapper {

    static PayoutPreferencesItemResponse mapToResponse(final PayoutPreferenceView view) {
        return new PayoutPreferencesItemResponse()
                .billingProfile(isNull(view.shortBillingProfileView()) ? null : billingProfileToResponse(view.shortBillingProfileView()))
                .project(projectToResponse(view.shortProjectView()));
    }

    private static ShortBillingProfileResponse billingProfileToResponse(final ShortBillingProfileView shortBillingProfileView) {
        return new ShortBillingProfileResponse()
                .id(shortBillingProfileView.getId().value())
                .name(shortBillingProfileView.getName())
                .type(BillingProfileMapper.map(shortBillingProfileView.getType()));
    }

    private static ShortProjectResponse projectToResponse(final ShortProjectView view) {
        return new ShortProjectResponse()
                .id(view.id().value())
                .logoUrl(view.logoUrl())
                .shortDescription(view.shortDescription())
                .slug(view.slug())
                .name(view.name());
    }
}
