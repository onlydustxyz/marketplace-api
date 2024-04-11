package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.ProjectLinkResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.backoffice.api.contract.model.SponsorPageItemResponse;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;
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

    static SponsorResponse map(Sponsor sponsor) {
        return new SponsorResponse()
                .id(sponsor.id())
                .name(sponsor.name())
                .url(sponsor.url())
                .logoUrl(sponsor.logoUrl());
    }

    static SponsorDetailsResponse mapToSponsorDetailsResponse(SponsorView sponsor, List<SponsorAccountStatement> accountStatements) {
        return new SponsorDetailsResponse()
                .id(sponsor.id())
                .name(sponsor.name())
                .url(sponsor.url())
                .logoUrl(sponsor.logoUrl())
                .projects(sponsor.projects().stream().map(p -> mapToProjectWithBudget(p, accountStatements)).toList())
                .availableBudgets(accountStatements.stream()
                        .map(SponsorMapper::mapSponsorBudgetResponse)
                        .collect(groupingBy(SponsorBudgetResponse::getCurrency, reducing(null, SponsorMapper::merge)))
                        .values().stream()
                        .sorted(comparing(b -> b.getCurrency().getCode()))
                        .toList());
    }

    private static ProjectWithBudgetResponse mapToProjectWithBudget(ShortProjectView project, List<SponsorAccountStatement> accountStatements) {
        return new ProjectWithBudgetResponse()
                .name(project.name())
                .logoUrl(project.logoUrl())
                .remainingBudgets(accountStatements.stream().map(statement -> {
                                    final var budget = statement.unspentBalanceSentTo(ProjectId.of(project.id().value())).getValue();
                                    final var currency = statement.account().currency();
                                    return new Money()
                                            .amount(budget)
                                            .currency(mapCurrency(currency))
                                            .usdEquivalent(currency.latestUsdQuote().map(r -> r.multiply(budget)).orElse(null));
                                }
                        )
                        .filter(money -> money.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .toList())
                ;
    }

    private static @NonNull SponsorBudgetResponse mapSponsorBudgetResponse(SponsorAccountStatement accountStatement) {
        return new SponsorBudgetResponse()
                .currency(mapCurrency(accountStatement.account().currency()))
                .currentAllowance(accountStatement.allowance().getValue());
    }

    private static @NonNull SponsorBudgetResponse merge(SponsorBudgetResponse left, @NonNull SponsorBudgetResponse right) {
        return left == null ? right : new SponsorBudgetResponse()
                .currency(right.getCurrency())
                .currentAllowance(left.getCurrentAllowance().add(right.getCurrentAllowance()))
                ;
    }
}
