package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.ProjectLinkResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.backoffice.api.contract.model.SponsorPageItemResponse;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface SponsorMapper {

    static SponsorPage sponsorPageToResponse(final Page<SponsorView> sponsorViewPage, int pageIndex) {
        return new SponsorPage()
                .sponsors(sponsorViewPage.getContent().stream().map(sponsor -> new SponsorPageItemResponse()
                        .id(sponsor.id().value())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                        .projects(sponsor.projects().stream().map(SponsorMapper::projectToBoResponse).toList())
                ).toList())
                .totalPageNumber(sponsorViewPage.getTotalPageNumber())
                .totalItemNumber(sponsorViewPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, sponsorViewPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, sponsorViewPage.getTotalPageNumber()));
    }

    static ProjectLinkResponse projectToBoResponse(final ShortProjectView view) {
        return new ProjectLinkResponse()
                .id(view.id().value())
                .slug(view.slug())
                .logoUrl(view.logoUrl())
                .name(view.name());
    }

    static onlydust.com.marketplace.api.contract.model.ProjectLinkResponse projectToResponse(final ShortProjectView view) {
        return new onlydust.com.marketplace.api.contract.model.ProjectLinkResponse()
                .id(view.id().value())
                .slug(view.slug())
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

    static HistoricalTransaction.Type map(SponsorAccountTransactionType type) {
        return switch (type) {
            case DEPOSIT -> HistoricalTransaction.Type.MINT; // Sponsor is interested on how much he can actually spend on projects
            case WITHDRAWAL -> HistoricalTransaction.Type.BURN;
            case ALLOCATION -> HistoricalTransaction.Type.TRANSFER;
            case UNALLOCATION -> HistoricalTransaction.Type.REFUND;
        };
    }

    static SponsorDetailsResponse mapToSponsorDetailsResponse(SponsorView sponsor, List<SponsorAccountStatement> accountStatements) {
        return new SponsorDetailsResponse()
                .id(sponsor.id().value())
                .name(sponsor.name())
                .url(sponsor.url())
                .logoUrl(sponsor.logoUrl())
                .projects(sponsor.projects().stream().map(p -> mapToProjectWithBudget(p, accountStatements)).toList())
                .availableBudgets(accountStatements.stream()
                        .map(SponsorMapper::mapAllowanceToMoney)
                        .collect(groupingBy(Money::getCurrency, reducing(null, MoneyMapper::add)))
                        .values().stream()
                        .sorted(comparing(b -> b.getCurrency().getCode()))
                        .toList());
    }

    private static ProjectWithBudgetResponse mapToProjectWithBudget(ShortProjectView project, List<SponsorAccountStatement> accountStatements) {
        final var budgets = accountStatements.stream().map(statement -> {
                            final var budget = statement.unspentBalanceSentTo(ProjectId.of(project.id().value())).getValue();
                            final var currency = statement.account().currency();
                            return new Money()
                                    .amount(budget)
                                    .prettyAmount(pretty(budget, currency.decimals(), currency.latestUsdQuote().orElse(null)))
                                    .currency(mapCurrency(currency))
                                    .usdEquivalent(currency.latestUsdQuote().map(r -> r.multiply(budget)).orElse(null));
                        }
                )
                .filter(money -> money.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        return new ProjectWithBudgetResponse()
                .id(project.id().value())
                .slug(project.slug())
                .name(project.name())
                .logoUrl(project.logoUrl())
                .totalUsdBudget(budgets.stream().map(Money::getUsdEquivalent).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
                .remainingBudgets(budgets)
                ;
    }

    private static @NonNull Money mapAllowanceToMoney(SponsorAccountStatement accountStatement) {
        return new Money()
                .currency(mapCurrency(accountStatement.account().currency()))
                .amount(accountStatement.allowance().getValue())
                .prettyAmount(pretty(accountStatement.allowance().getValue(),
                        accountStatement.account().currency().decimals(),
                        accountStatement.account().currency().latestUsdQuote().orElse(null)))
                .usdEquivalent(accountStatement.account().currency().latestUsdQuote()
                        .map(q -> q.multiply(accountStatement.allowance().getValue()))
                        .orElse(null));
    }

    static TransactionHistoryPageResponse mapTransactionHistory(Page<HistoricalTransaction> page, int pageIndex) {
        return new TransactionHistoryPageResponse()
                .transactions(page.getContent().stream().map(SponsorMapper::mapHistoricalTransaction).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static TransactionHistoryPageItemResponse mapHistoricalTransaction(HistoricalTransaction historicalTransaction) {
        return new TransactionHistoryPageItemResponse()
                .id(historicalTransaction.id())
                .date(historicalTransaction.timestamp())
                .type(mapTransactionType(historicalTransaction))
                .project(historicalTransaction.project() == null ? null : projectToResponse(historicalTransaction.project()))
                .amount(new Money()
                        .amount(historicalTransaction.amount().getValue())
                        .currency(mapCurrency(historicalTransaction.sponsorAccount().currency()))
                );
    }

    static SponsorAccountTransactionType mapTransactionType(HistoricalTransaction transaction) {
        return switch (transaction.type()) {
            case MINT -> SponsorAccountTransactionType.DEPOSIT;
            case BURN -> SponsorAccountTransactionType.WITHDRAWAL;
            case TRANSFER -> SponsorAccountTransactionType.ALLOCATION;
            case REFUND -> SponsorAccountTransactionType.UNALLOCATION;
            default -> throw OnlyDustException.internalServerError("Unexpected transaction type: %s".formatted(transaction.type()));
        };
    }

    static HistoricalTransaction.Sort parseTransactionSort(@NonNull String sort) {
        return switch (sort) {
            case "DATE" -> HistoricalTransaction.Sort.DATE;
            case "TYPE" -> HistoricalTransaction.Sort.TYPE;
            case "AMOUNT" -> HistoricalTransaction.Sort.AMOUNT;
            case "PROJECT" -> HistoricalTransaction.Sort.PROJECT;
            default -> throw OnlyDustException.badRequest("Invalid sort parameter: %s".formatted(sort));
        };
    }
}
