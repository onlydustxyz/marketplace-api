package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertEquals(name, committeeArgumentCaptor.getValue().name());
        assertEquals(endDate, committeeArgumentCaptor.getValue().endDate());
        assertEquals(startDate, committeeArgumentCaptor.getValue().startDate());
        assertEquals(Committee.Status.DRAFT, committeeArgumentCaptor.getValue().status());
        assertNotNull(committeeArgumentCaptor.getValue().id());
    }

    @Test
    void should_get_committee_by_id() {
        // Given
        final CommitteeStoragePort committeeStoragePort = mock(CommitteeStoragePort.class);
        final CommitteeService committeeService = new CommitteeService(committeeStoragePort);
        final Committee.Id committeeId = Committee.Id.random();
        final String name = faker.rickAndMorty().character();
        final ZonedDateTime startDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final ZonedDateTime endDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final CommitteeView committeeView = CommitteeView.builder()
                .id(Committee.Id.random())
                .endDate(endDate)
                .startDate(startDate)
                .name(name)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .build();

        // When
        when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committeeView));
        final CommitteeView committeeById = committeeService.getCommitteeById(committeeId);

        // Then
        assertEquals(committeeView, committeeById);
    }

    @Test
    void should_throw_not_found_given_a_committee_not_found_by_id() {
        // Given
        final CommitteeStoragePort committeeStoragePort = mock(CommitteeStoragePort.class);
        final CommitteeService committeeService = new CommitteeService(committeeStoragePort);

        // When
        when(committeeStoragePort.findById(any())).thenReturn(Optional.empty());

        // Then
        assertThrows(OnlyDustException.class, () -> committeeService.getCommitteeById(Committee.Id.random()));
    }
}
