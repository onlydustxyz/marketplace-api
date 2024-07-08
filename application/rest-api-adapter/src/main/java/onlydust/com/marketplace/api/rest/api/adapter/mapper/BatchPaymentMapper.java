package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.BatchPaymentResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentStatus;
import onlydust.com.backoffice.api.contract.model.BatchPaymentsResponse;
import onlydust.com.backoffice.api.contract.model.TotalMoneyWithUsdEquivalentResponse;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;

import java.math.BigDecimal;
import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapNetwork;

public interface BatchPaymentMapper {

    static BatchPaymentsResponse domainToResponse(final List<BatchPaymentShortView> batchPayments) {
        final BatchPaymentsResponse batchPaymentsResponse = new BatchPaymentsResponse();
        batchPayments.stream().map(BatchPaymentMapper::domainToResponse).forEach(batchPaymentsResponse::addBatchPaymentsItem);
        return batchPaymentsResponse;
    }

    static BatchPaymentResponse domainToResponse(final BatchPaymentShortView bp) {
        final var totalsPerCurrency = bp.totalsPerCurrency().stream().map(BackOfficeMapper::totalMoneyViewToResponse).toList();
        return new BatchPaymentResponse()
                .id(bp.id().value())
                .createdAt(bp.createdAt())
                .status(map(bp.status()))
                .rewardCount(bp.rewardCount())
                .network(mapNetwork(bp.network()))
                .totalUsdEquivalent(totalsPerCurrency.stream()
                        .map(TotalMoneyWithUsdEquivalentResponse::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalsPerCurrency(totalsPerCurrency);
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
