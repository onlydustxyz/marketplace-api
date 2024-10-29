package onlydust.com.marketplace.api.near.dto;

public enum TxExecutionStatus {
    /// Transaction is waiting to be included into the block
    NONE,
    /// Transaction is included into the block. The block may be not finalized yet
    INCLUDED,
    /// Transaction is included into the block +
    /// All non-refund transaction receipts finished their execution.
    /// The corresponding blocks for tx and each receipt may be not finalized yet
    EXECUTED_OPTIMISTIC,
    /// Transaction is included into finalized block
    INCLUDED_FINAL,
    /// Transaction is included into finalized block +
    /// All non-refund transaction receipts finished their execution.
    /// The corresponding blocks for each receipt may be not finalized yet
    EXECUTED,
    /// Transaction is included into finalized block +
    /// Execution of all transaction receipts is finalized, including refund receipts
    FINAL
}
