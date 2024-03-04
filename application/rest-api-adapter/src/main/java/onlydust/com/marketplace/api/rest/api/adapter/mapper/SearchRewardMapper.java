package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;

public interface SearchRewardMapper {

    static SearchRewardsResponse searchRewardToResponse(final List<RewardView> rewardViews) {
        final SearchRewardsResponse searchRewardsResponse = new SearchRewardsResponse();
        for (RewardView view : rewardViews) {
            searchRewardsResponse.addRewardsItem(new SearchRewardItemResponse()
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
                    .billingProfile(new BillingProfileResponse()
                            .id(view.billingProfileAdmin().billingProfileId().value())
                            .type(switch (view.billingProfileAdmin().billingProfileType()) {
                                case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                                case COMPANY, SELF_EMPLOYED -> BillingProfileType.COMPANY;
                            })
                            .name(view.billingProfileAdmin().billingProfileName())
                            .admins(List.of(new BillingProfileAdminResponse()
                                            .name(view.billingProfileAdmin().adminName())
                                            .email(view.billingProfileAdmin().adminEmail())
                                            .login(view.billingProfileAdmin().adminGithubLogin())
                                            .avatarUrl(view.billingProfileAdmin().adminGithubAvatarUrl())
                                    )
                            )));
        }
        return searchRewardsResponse;
    }

    static MoneyLinkResponse moneyViewToResponse(final MoneyView view) {
        return new MoneyLinkResponse()
                .amount(view.amount())
                .currencyCode(view.currencyCode())
                .currencyName(view.currencyName())
                .currencyLogoUrl(view.currencyLogoUrl())
                .dollarsEquivalent(view.dollarsEquivalent());
    }
}
