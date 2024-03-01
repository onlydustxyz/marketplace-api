package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
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
                    .money(new MoneyLinkResponse()
                            .amount(view.money().amount())
                            .currencyCode(view.money().currencyCode())
                            .currencyName(view.money().currencyName())
                            .currencyLogoUrl(view.money().currencyLogoUrl())
                            .dollarsEquivalent(view.money().dollarsEquivalent())
                    )
                    .project(new ProjectLinkResponse()
                            .name(view.projectName())
                            .logoUrl(view.projectLogoUrl()))
                    .sponsors(view.sponsors().stream()
                            .map(shortSponsorView -> new SponsorLinkResponse()
                                    .name(shortSponsorView.name())
                                    .avatarUrl(shortSponsorView.logoUrl()))
                            .toList())
                    .billingProfileAdmin(new BillingProfileAdminResponse()
                            .billingProfileName(view.billingProfileAdmin().billingProfileName())
                            .billingProfileType(switch (view.billingProfileAdmin().billingProfileType()) {
                                case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                                case COMPANY, SELF_EMPLOYED -> BillingProfileType.COMPANY;
                            })
                            .adminName(view.billingProfileAdmin().adminName())
                            .adminEmail(view.billingProfileAdmin().adminEmail())
                            .adminLogin(view.billingProfileAdmin().adminGithubLogin())
                            .adminAvatarUrl(view.billingProfileAdmin().adminGithubAvatarUrl())
                    ));
        }
        return searchRewardsResponse;
    }
}
