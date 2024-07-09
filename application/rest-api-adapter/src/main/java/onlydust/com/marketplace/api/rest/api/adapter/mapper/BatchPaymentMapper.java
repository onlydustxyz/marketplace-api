package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.BatchPaymentShortResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentStatus;
import onlydust.com.backoffice.api.contract.model.BatchPaymentsResponse;
import onlydust.com.marketplace.accounting.domain.model.Payment;

import java.time.ZoneOffset;
import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapNetwork;

public interface BatchPaymentMapper {

    static BatchPaymentsResponse map(final List<Payment> batchPayments) {
        return new BatchPaymentsResponse()
                .batchPayments(batchPayments.stream().map(BatchPaymentMapper::map).toList());
    }

    static BatchPaymentShortResponse map(final Payment bp) {
        return new BatchPaymentShortResponse()
                .id(bp.id().value())
                .createdAt(bp.createdAt().toInstant().atZone(ZoneOffset.UTC))
                .status(map(bp.status()))
                .rewardCount((long) bp.rewards().size())
                .network(mapNetwork(bp.network()));
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
