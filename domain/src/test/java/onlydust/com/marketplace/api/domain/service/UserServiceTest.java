package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_find_user_profile_given_an_id() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort);
        final UUID userId = UUID.randomUUID();
        final UserProfileView userProfileView = UserProfileView.builder()
                .id(userId)
                .avatarUrl(faker.pokemon().name())
                .githubId(faker.number().randomNumber())
                .login(faker.hacker().verb())
                .build();

        // When
        when(userStoragePort.getProfileById(userId))
                .thenReturn(userProfileView);
        final UserProfileView profileById = userService.getProfileById(userId);

        // Then
        assertEquals(userProfileView, profileById);
    }
}
