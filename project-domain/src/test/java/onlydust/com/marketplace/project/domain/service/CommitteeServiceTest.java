package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommitteeServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_a_committee() {
        // Given
        final CommitteeStoragePort committeeStoragePort = mock(CommitteeStoragePort.class);
        final CommitteeService committeeService = new CommitteeService(committeeStoragePort);
        final String name = faker.rickAndMorty().character();
        final ZonedDateTime startDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final ZonedDateTime endDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());

        // When
        committeeService.createCommittee(name, startDate,
                endDate);

        // Then
        final ArgumentCaptor<Committee> committeeArgumentCaptor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeStoragePort).save(committeeArgumentCaptor.capture());
        Assertions.assertEquals(name, committeeArgumentCaptor.getValue().name());
        Assertions.assertEquals(endDate, committeeArgumentCaptor.getValue().endDate());
        Assertions.assertEquals(startDate, committeeArgumentCaptor.getValue().startDate());
        Assertions.assertEquals(Committee.Status.DRAFT, committeeArgumentCaptor.getValue().status());
        Assertions.assertNotNull(committeeArgumentCaptor.getValue().id());
    }
}
