package onlydust.com.marketplace.api.helper;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CurrencyHelper {

    public static final Currency.Id BTC = Currency.Id.of("3f6e1c98-8659-493a-b941-943a803bd91f");
    public static final Currency.Id STRK = Currency.Id.of("81b7e948-954f-4718-bad3-b70a0edd27e1");
    public static final Currency.Id USD = Currency.Id.of("f35155b5-6107-4677-85ac-23f8c2a63193");
    public static final Currency.Id USDC = Currency.Id.of("562bbf65-8a71-4d30-ad63-520c0d68ba27");
    public static final Currency.Id OP = Currency.Id.of("00ca98a5-0197-4b76-a208-4bfc55ea8256");
    public static final Currency.Id ETH = Currency.Id.of("71bdfcf4-74ee-486b-8cfe-5d841dd93d5c");
    public static final Currency.Id APT = Currency.Id.of("48388edb-fda2-4a32-b228-28152a147500");

    @Autowired
    private final CurrencyFacadePort currencyFacadePort;

    public Currency addERC20Support(Blockchain blockchain, Currency.Code code) {
        return currencyFacadePort.addERC20Support(blockchain, switch (blockchain) {
            case STARKNET -> switch (code.toString()) {
                case Currency.Code.USDC_STR -> StarkNet.contractAddress("0x53c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8");
                default -> throw new IllegalArgumentException("Unsupported ERC20 token in tests");
            };
            default -> throw new IllegalArgumentException("Unsupported blockchain in tests");
        });
    }
}
