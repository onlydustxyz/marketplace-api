package onlydust.com.marketplace.api.helper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Builder
public class WiremockServerRegistration {
    private final @NonNull String beanName;
    private final @NonNull String stubLocation;
    private final @NonNull String property;
    private final String recordFrom;

    public void register(final @NonNull ConfigurableApplicationContext context) {
        final var wireMockServer = new WireMockServer(
                options()
                        .dynamicPort()
                        .globalTemplating(true)
                        .usingFilesUnderClasspath("wiremock/" + stubLocation)
        );

        wireMockServer.start();

        if (recordFrom != null)
            wireMockServer.startRecording(recordFrom);

        context.getBeanFactory().registerSingleton(beanName, wireMockServer);

        TestPropertyValues.of("%s:http://localhost:%d".formatted(property, wireMockServer.port()))
                .applyTo(context);

        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                if (wireMockServer.getRecordingStatus().getStatus() == RecordingStatus.Recording)
                    wireMockServer.stopRecording();
                wireMockServer.stop();
            }
        });
    }
}
