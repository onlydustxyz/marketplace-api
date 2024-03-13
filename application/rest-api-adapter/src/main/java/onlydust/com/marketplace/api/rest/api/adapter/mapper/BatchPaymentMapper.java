package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapNetwork;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BatchPaymentMapper {

    static BatchPaymentsResponse domainToResponse(final List<BatchPayment> batchPayments) {
        final BatchPaymentsResponse batchPaymentsResponse = new BatchPaymentsResponse();
        for (BatchPayment batchPayment : batchPayments) {
            batchPaymentsResponse.addBatchPaymentsItem(new BatchPaymentResponse()
                    .id(batchPayment.id().value())
                    .csv(batchPayment.csv())
                    .rewardCount((long) batchPayment.rewardIds().size())
                    .network(mapNetwork(batchPayment.network()))
                    .totalAmounts(batchPayment.moneys().stream().map(SearchRewardMapper::moneyViewToResponse).toList())
            );
        }
        return batchPaymentsResponse;
    }

    static BatchPaymentPageResponse pageToResponse(final Page<BatchPayment> page, final int pageIndex) {
        return new BatchPaymentPageResponse()
                .batchPayments(page.getContent().stream().map(BatchPaymentMapper::itemToResponse).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static BatchPaymentItemResponse itemToResponse(final BatchPayment batchPayment) {
        BigDecimal totalDollarsEquivalent = batchPayment.moneys().stream()
                .map(MoneyView::dollarsEquivalent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BatchPaymentItemResponse()
                .id(batchPayment.id().value())
                .rewardCount((long) batchPayment.rewardIds().size())
                .network(mapNetwork(batchPayment.network()))
                .createdAt(DateMapper.toZoneDateTime(batchPayment.createdAt()))
                .totalAmountUsd(totalDollarsEquivalent)
                .totalAmounts(batchPayment.moneys().stream().map(SearchRewardMapper::moneyViewToResponse).toList());
    }

    static BatchPaymentDetailsResponse detailsToResponse(final BatchPaymentDetailsView batchPaymentDetailsView) {
        BigDecimal totalDollarsEquivalent = batchPaymentDetailsView.batchPayment().moneys().stream()
                .map(MoneyView::dollarsEquivalent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new BatchPaymentDetailsResponse()
                .id(batchPaymentDetailsView.batchPayment().id().value())
                .csv(batchPaymentDetailsView.batchPayment().csv())
                .transactionHash(batchPaymentDetailsView.batchPayment().transactionHash())
                .totalAmountUsd(totalDollarsEquivalent)
                .rewardCount((long) batchPaymentDetailsView.batchPayment().rewardIds().size())
                .network(mapNetwork(batchPaymentDetailsView.batchPayment().network()))
                .createdAt(DateMapper.toZoneDateTime(batchPaymentDetailsView.batchPayment().createdAt()))
                .rewards(batchPaymentDetailsView.rewardViews().stream().map(SearchRewardMapper::mapToItem).toList())
                .totalAmounts(batchPaymentDetailsView.batchPayment().moneys().stream().map(SearchRewardMapper::moneyViewToResponse).toList());
    }
}
