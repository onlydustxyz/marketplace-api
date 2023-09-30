package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.UserProfile;
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
        final UserProfile userProfile = UserProfile.builder()
                .id(userId)
                .avatarUrl(faker.pokemon().name())
                .githubId(faker.number().randomDigit())
                .login(faker.hacker().verb())
                .build();

        // When
        when(userStoragePort.getProfileById(userId))
                .thenReturn(userProfile);
        final UserProfile profileById = userService.getProfileById(userId);

        // Then
        assertEquals(userProfile, profileById);
    }
}
