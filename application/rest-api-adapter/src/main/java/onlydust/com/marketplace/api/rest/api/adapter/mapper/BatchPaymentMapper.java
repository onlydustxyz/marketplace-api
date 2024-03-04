package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.BatchPaymentResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentsResponse;
import onlydust.com.backoffice.api.contract.model.BlockchainContract;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;

import java.util.List;

public interface BatchPaymentMapper {

    static BatchPaymentsResponse domainToResponse(final List<BatchPayment> batchPayments) {
        final BatchPaymentsResponse batchPaymentsResponse = new BatchPaymentsResponse();
        for (BatchPayment batchPayment : batchPayments) {
            batchPaymentsResponse.addBatchPaymentsItem(new BatchPaymentResponse()
                    .id(batchPayment.id().value())
                    .csv(batchPayment.csv())
                    .rewardCount(batchPayment.rewardCount())
                    .blockchain(switch (batchPayment.blockchain()) {
                        case ETHEREUM -> BlockchainContract.ETHEREUM;
                        case OPTIMISM -> BlockchainContract.OPTIMISM;
                        case STARKNET -> BlockchainContract.STARKNET;
                        case APTOS -> BlockchainContract.APTOS;
                    })
                    .totalAmounts(batchPayment.moneys().stream().map(SearchRewardMapper::moneyViewToResponse).toList())
            );
        }
        return batchPaymentsResponse;
    }
}
