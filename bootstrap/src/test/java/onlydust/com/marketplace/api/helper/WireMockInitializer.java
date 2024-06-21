package onlydust.com.marketplace.api.helper;

import lombok.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(final @NonNull ConfigurableApplicationContext context) {
        WiremockServerRegistration.builder()
                .beanName("ethereumWireMockServer")
                .stubLocation("ethereum")
                .property("infrastructure.ethereum.base-uri")
//                    .recordFrom("https://mainnet.infura.io/v3")
                .build()
                .register(context);

        WiremockServerRegistration.builder()
                .beanName("optimismWireMockServer")
                .stubLocation("optimism")
                .property("infrastructure.optimism.base-uri")
                .build()
                .register(context);

        WiremockServerRegistration.builder()
                .beanName("starknetWireMockServer")
                .stubLocation("starknet")
                .property("infrastructure.starknet.base-uri")
//                .recordFrom("https://starknet-mainnet.infura.io/v3")
                .build()
                .register(context);

        WiremockServerRegistration.builder()
                .beanName("aptosWireMockServer")
                .stubLocation("aptos")
                .property("infrastructure.aptos.base-uri")
                .build()
                .register(context);

        WiremockServerRegistration.builder()
                .beanName("coinmarketcapWireMockServer")
                .stubLocation("coinmarketcap")
                .property("infrastructure.coinmarketcap.base-uri")
                .build()
                .register(context);
    }
}
