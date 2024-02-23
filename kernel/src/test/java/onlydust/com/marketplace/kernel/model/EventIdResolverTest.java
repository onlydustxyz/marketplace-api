package onlydust.com.marketplace.kernel.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

class EventIdResolverTest {

    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestEventContainer implements Serializable {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(EventIdResolver.class)
        private TestEvent testEvent;
    }

    public static abstract class TestEvent {
    }

    @EventType("A")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubATestEvent extends TestEvent {
        public String foo;
    }

    @EventType("B")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubBTestEvent extends TestEvent {
        public String bar;
    }

    @Test
    void should_serialize_with_type_id() throws JsonProcessingException {
        final var testContainer = new TestEventContainer(new SubATestEvent("yolo"));
        final var mapper = new ObjectMapper();
        final var serialized = mapper.writeValueAsString(testContainer);

        assertThat(serialized).isEqualTo("{\"testEvent\":{\"@type\":\"A\",\"foo\":\"yolo\"}}");
    }

    @Test
    void should_deserialize_with_type_id() throws JsonProcessingException {
        final var mapper = new ObjectMapper();
        final var deserialized = mapper.readValue("{\"testEvent\":{\"@type\":\"B\",\"bar\":\"yolo\"}}", TestEventContainer.class);

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.testEvent).isInstanceOf(SubBTestEvent.class);
        assertThat(((SubBTestEvent) deserialized.testEvent).bar).isEqualTo("yolo");
    }
}