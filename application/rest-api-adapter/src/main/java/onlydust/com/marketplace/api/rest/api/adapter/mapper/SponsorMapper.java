package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.ProjectLinkResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.backoffice.api.contract.model.SponsorPageItemResponse;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper.toMoney;
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

    static ProjectLinkResponse projectToBoResponse(final ProjectShortView view) {
        return new ProjectLinkResponse()
                .id(view.id().value())
                .slug(view.slug())
                .logoUrl(view.logoUrl())
                .name(view.name());
    }

    static onlydust.com.marketplace.api.contract.model.ProjectLinkResponse projectToResponse(final ProjectShortView view) {
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
            // Sponsor is interested on how much he can actually spend on projects
            case DEPOSIT -> HistoricalTransaction.Type.MINT;
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
                .projects(sponsor.projects().stream()
                        .map(p -> mapToProjectWithBudget(p, accountStatements))
                        .sorted(comparing(ProjectWithBudgetResponse::getName))
                        .toList())
                .availableBudgets(sponsorAccountStatementsToBudgets(accountStatements).values().stream()
                        .map(MoneyMapper::toMoney)
                        .sorted(comparing(b -> b.getCurrency().getCode()))
                        .toList());
    }

    private static Map<Currency, MoneyView> sponsorAccountStatementsToBudgets(List<SponsorAccountStatement> accountStatements) {
        return accountStatements.stream()
                .map(account -> new MoneyView(account.allowance().getValue(), account.account().currency()))
                .collect(
                        groupingBy(MoneyView::getCurrency,
                                reducing(null, (money1, money2) -> new MoneyView(
                                        isNull(money1) ? money2.getAmount() : money1.getAmount().add(money2.getAmount()),
                                        money2.getCurrency()
                                ))));
    }

    private static ProjectWithBudgetResponse mapToProjectWithBudget(ProjectShortView project, List<SponsorAccountStatement> accountStatements) {
        final var budgets = accountStatements.stream().map(statement -> {
                            final var budget = statement.unspentBalanceSentTo(ProjectId.of(project.id().value())).getValue();
                            final var currency = statement.account().currency();
                            return toMoney(budget, currency);
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
                .amount(toMoney(historicalTransaction.amount().getValue(), historicalTransaction.currency()));
    }

    static SponsorAccountTransactionType mapTransactionType(HistoricalTransaction transaction) {
        return switch (transaction.type()) {
            case MINT -> SponsorAccountTransactionType.DEPOSIT;
            case BURN -> SponsorAccountTransactionType.WITHDRAWAL;
            case TRANSFER -> SponsorAccountTransactionType.ALLOCATION;
            case REFUND -> SponsorAccountTransactionType.UNALLOCATION;
            default -> throw OnlyDustException
                    .internalServerError("Unexpected transaction type: %s".formatted(transaction.type()));
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
