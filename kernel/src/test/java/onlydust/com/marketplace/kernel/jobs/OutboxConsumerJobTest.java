package onlydust.com.marketplace.kernel.jobs;

import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OutboxConsumerJobTest {

    OutboxPortStub outbox;
    OutboxConsumer consumer;
    OutboxConsumerJob job;

    @EventType("TestEvent")
    @EqualsAndHashCode(callSuper = false)
    public static class TestEvent extends Event {
        final UUID id = UUID.randomUUID();
    }

    @BeforeEach
    void setUp() {
        outbox = spy(new OutboxPortStub());
        consumer = mock(OutboxConsumer.class);
        job = new OutboxConsumerJob(outbox, consumer);
    }

    @Test
    void should_do_nothing_when_there_is_no_event() {
        // When
        when(outbox.peek()).thenReturn(Optional.empty());
        job.run();

        // Then
        verify(outbox, never()).ack(any());
        verify(outbox, never()).skip(any(), any());
        verify(outbox, never()).nack(any(), any());
        verify(consumer, never()).process(any());
    }

    @Test
    void should_process_one_event() {
        // When
        final var event = newTestEvent(1L);
        job.run();

        // Then
        verify(outbox).ack(eq(1L));
        verify(outbox, never()).skip(any(), any());
        verify(outbox, never()).nack(any(), any());
        verify(consumer).process(eq(event));
    }


    @Test
    void should_process_multiple_events() {
        // When
        final var event1 = newTestEvent(1L);
        final var event2 = newTestEvent(2L);
        final var event3 = newTestEvent(3L);
        job.run();

        // Then
        verify(outbox).ack(eq(1L));
        verify(outbox).ack(eq(2L));
        verify(outbox).ack(eq(3L));
        verify(outbox, never()).skip(any(), any());
        verify(outbox, never()).nack(any(), any());
        verify(consumer).process(eq(event1));
        verify(consumer).process(eq(event2));
        verify(consumer).process(eq(event3));
    }

    @Test
    void should_fail_processing_one_event() {
        // When
        final var event = newTestEvent(1L);
        doThrow(new RuntimeException("Test exception")).when(consumer).process(event);

        assertThatThrownBy(() -> job.run())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");

        // Then
        verify(outbox, never()).ack(any());
        verify(outbox, never()).skip(any(), any());
        verify(outbox).nack(eq(1L), eq("Test exception"));
        verify(consumer).process(eq(event));
    }

    @Test
    void should_fail_processing_one_event_among_multiple_events() {
        // When
        final var event1 = newTestEvent(1L);
        final var event2 = newTestEvent(2L);
        final var event3 = newTestEvent(3L);
        doThrow(new RuntimeException("Test exception")).when(consumer).process(event2);

        assertThatThrownBy(() -> job.run())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");

        // Then
        verify(outbox).ack(eq(1L));
        verify(outbox, never()).ack(eq(2L));
        verify(outbox, never()).ack(eq(3L));
        verify(outbox, never()).skip(any(), any());
        verify(outbox).nack(eq(2L), eq("Test exception"));
        verify(outbox, never()).nack(eq(1L), any());
        verify(outbox, never()).nack(eq(3L), any());
        verify(consumer).process(eq(event1));
        verify(consumer).process(eq(event2));
        verify(consumer, never()).process(eq(event3));
    }

    @Test
    void should_skip_processing_one_event() {
        // When
        final var event = newTestEvent(1L);
        doThrow(new OutboxSkippingException("Test exception")).when(consumer).process(event);
        job.run();

        // Then
        verify(outbox, never()).ack(any());
        verify(outbox).skip(eq(1L), eq("Test exception"));
        verify(outbox, never()).nack(any(), any());
        verify(consumer).process(eq(event));
    }

    @Test
    void should_skip_processing_one_event_among_multiple_events() {
        // When
        final var event1 = newTestEvent(1L);
        final var event2 = newTestEvent(2L);
        final var event3 = newTestEvent(3L);
        doThrow(new OutboxSkippingException("Test exception")).when(consumer).process(event2);
        job.run();

        // Then
        verify(outbox).ack(eq(1L));
        verify(outbox, never()).ack(eq(2L));
        verify(outbox).ack(eq(3L));
        verify(outbox, never()).skip(eq(1L), any());
        verify(outbox).skip(eq(2L), eq("Test exception"));
        verify(outbox, never()).skip(eq(3L), any());
        verify(outbox, never()).nack(any(), any());
        verify(consumer).process(eq(event1));
        verify(consumer).process(eq(event2));
        verify(consumer).process(eq(event3));
    }


    private TestEvent newTestEvent(final long id) {
        final var event = new TestEvent();
        outbox.addEvent(new OutboxPort.IdentifiableEvent(id, event));
        return event;
    }

    static class OutboxPortStub implements OutboxPort {
        private final Queue<IdentifiableEvent> events = new LinkedList<>();

        public void addEvent(IdentifiableEvent event) {
            events.add(event);
        }

        @Override
        public void push(Event event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IdentifiableEvent> peek() {
            return Optional.ofNullable(events.poll());
        }

        @Override
        public void ack(Long eventId) {
        }

        @Override
        public void nack(Long eventId, String message) {
        }

        @Override
        public void skip(Long eventId, String someReasonToSkip) {
        }
    }
}