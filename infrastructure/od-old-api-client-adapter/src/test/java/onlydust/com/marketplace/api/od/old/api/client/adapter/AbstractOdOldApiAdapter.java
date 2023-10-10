package onlydust.com.marketplace.api.od.old.api.client.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"it"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = OdOldApiAdapterITApplication.class)
@ContextConfiguration(initializers = AbstractOdOldApiAdapter.WireMockInitializer.class)
@EnableConfigurationProperties
public abstract class AbstractOdOldApiAdapter {

    @Autowired
    protected WebTestClient client;

    @Autowired
    protected WireMockServer odOldApiAdapterWireMockServer;

    @Autowired
    protected ObjectMapper objectMapper;
    protected final static Faker FAKER = new Faker();


    public static class WireMockInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            WireMockServer odOldApiAdapterWireMockServer =
                    new WireMockServer(new WireMockConfiguration().dynamicPort());
            odOldApiAdapterWireMockServer.start();

            configurableApplicationContext
                    .getBeanFactory()
                    .registerSingleton("odOldApiAdapterWireMockServer", odOldApiAdapterWireMockServer);

            configurableApplicationContext.addApplicationListener(
                    applicationEvent -> {
                        if (applicationEvent instanceof ContextClosedEvent) {
                            odOldApiAdapterWireMockServer.stop();
                        }
                    });

            TestPropertyValues.of(
                            "od-old-api-client-adapter.base-url:http://localhost:" + odOldApiAdapterWireMockServer.port() + "/")
                    .applyTo(configurableApplicationContext);
        }
    }

    protected <T> T getStubsFromClassT(final String testResourcesDir, final String fileName, final Class<T> tClass) throws IOException {
        final String dto1 = Files.readString(Paths.get("target/test-classes/" + testResourcesDir + "/" + fileName));
        return objectMapper.readValue(dto1, tClass);
    }
}
