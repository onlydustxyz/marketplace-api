package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapNetwork;
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
        final var totalsPerCurrency = bp.totalsPerCurrency().stream().map(SearchRewardMapper::totalMoneyViewToResponse).toList();
        return new BatchPaymentResponse()
                .id(bp.batchPayment().id().value())
                .createdAt(DateMapper.toZoneDateTime(bp.batchPayment().createdAt()))
                .status(map(bp.batchPayment().status()))
                .csv(bp.batchPayment().csv())
                .rewardCount((long) bp.rewardViews().size())
                .network(mapNetwork(bp.batchPayment().network()))
                .totalUsdEquivalent(totalsPerCurrency.stream()
                        .map(TotalMoneyWithUsdEquivalentResponse::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalsPerCurrency(totalsPerCurrency);
    }

    static BatchPaymentDetailsResponse domainToDetailedResponse(final BatchPaymentDetailsView bp) {
        final var totalsPerCurrency = bp.totalsPerCurrency().stream().map(SearchRewardMapper::totalMoneyViewToResponse).toList();
        return new BatchPaymentDetailsResponse()
                .id(bp.batchPayment().id().value())
                .createdAt(DateMapper.toZoneDateTime(bp.batchPayment().createdAt()))
                .status(map(bp.batchPayment().status()))
                .csv(bp.batchPayment().csv())
                .rewardCount((long) bp.rewardViews().size())
                .network(mapNetwork(bp.batchPayment().network()))
                .totalUsdEquivalent(totalsPerCurrency.stream()
                        .map(TotalMoneyWithUsdEquivalentResponse::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalsPerCurrency(totalsPerCurrency)
                .transactionHash(bp.batchPayment().transactionHash())
                .rewards(bp.rewardViews().stream().map(SearchRewardMapper::mapToItem).toList());
    }

    static BatchPayment.Status map(final BatchPaymentStatus status) {
        return switch (status) {
            case TO_PAY -> BatchPayment.Status.TO_PAY;
            case PAID -> BatchPayment.Status.PAID;
        };
    }

    static BatchPaymentStatus map(final BatchPayment.Status status) {
        return switch (status) {
            case TO_PAY -> BatchPaymentStatus.TO_PAY;
            case PAID -> BatchPaymentStatus.PAID;
        };
    }
}
