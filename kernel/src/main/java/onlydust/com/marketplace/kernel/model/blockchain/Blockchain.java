package onlydust.com.marketplace.kernel.model.blockchain;

public enum Blockchain {
    ETHEREUM, OPTIMISM, STARKNET, APTOS;

    public boolean isEvmCompatible() {
        return switch (this) {
            case ETHEREUM, OPTIMISM -> true;
            default -> false;
        };
    }
}
