package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapNetwork;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapToShortResponse;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BatchPaymentMapper {

    static BatchPaymentsResponse domainToResponse(final List<BatchPaymentDetailsView> batchPayments) {
        final BatchPaymentsResponse batchPaymentsResponse = new BatchPaymentsResponse();
        batchPayments.stream().map(BatchPaymentMapper::domainToResponse).forEach(batchPaymentsResponse::addBatchPaymentsItem);
        return batchPaymentsResponse;
    }

    static BatchPaymentPageResponse pageToResponse(final Page<BatchPaymentDetailsView> page, final int pageIndex) {
        return new BatchPaymentPageResponse()
                .batchPayments(page.getContent().stream().map(BatchPaymentMapper::domainToResponse).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static BatchPaymentResponse domainToResponse(final BatchPaymentDetailsView bp) {
        final var totalsPerCurrency = bp.totalsPerCurrency().stream().map(BackOfficeMapper::totalMoneyViewToResponse).toList();
        return new BatchPaymentResponse()
                .id(bp.payment().id().value())
                .createdAt(DateMapper.toZoneDateTime(bp.payment().createdAt()))
                .status(map(bp.payment().status()))
                .rewardCount((long) bp.rewardViews().size())
                .network(mapNetwork(bp.payment().network()))
                .totalUsdEquivalent(totalsPerCurrency.stream()
                        .map(TotalMoneyWithUsdEquivalentResponse::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalsPerCurrency(totalsPerCurrency);
    }

    static BatchPaymentDetailsResponse domainToDetailedResponse(final BatchPaymentDetailsView bp, final AuthenticatedUser authenticatedUser) {
        final var totalsPerCurrency = bp.totalsPerCurrency().stream().map(BackOfficeMapper::totalMoneyViewToResponse).toList();
        return new BatchPaymentDetailsResponse()
                .id(bp.payment().id().value())
                .createdAt(DateMapper.toZoneDateTime(bp.payment().createdAt()))
                .status(map(bp.payment().status()))
                .csv(bp.payment().csv())
                .rewardCount((long) bp.rewardViews().size())
                .network(mapNetwork(bp.payment().network()))
                .totalUsdEquivalent(totalsPerCurrency.stream()
                        .map(TotalMoneyWithUsdEquivalentResponse::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalsPerCurrency(totalsPerCurrency)
                .transactionHash(bp.payment().transactionHash())
                .rewards(bp.rewardViews().stream().map(r -> mapToShortResponse(r, authenticatedUser)).toList());
    }

    static Payment.Status map(final BatchPaymentStatus status) {
        return switch (status) {
            case TO_PAY -> Payment.Status.TO_PAY;
            case PAID -> Payment.Status.PAID;
        };
    }

    static BatchPaymentStatus map(final Payment.Status status) {
        return switch (status) {
            case TO_PAY -> BatchPaymentStatus.TO_PAY;
            case PAID -> BatchPaymentStatus.PAID;
        };
    }
}
