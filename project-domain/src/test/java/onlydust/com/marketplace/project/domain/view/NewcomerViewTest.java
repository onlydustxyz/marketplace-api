package onlydust.com.marketplace.project.domain.view;

import onlydust.com.marketplace.project.domain.model.UserProfileCover;
import org.junit.jupiter.api.Test;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class NewcomerViewTest {

    @Test
    void should_get_random_cover_by_default() {
        // Given a looooooot of covers
        final var covers = LongStream.rangeClosed(1, UserProfileCover.values().length * 10L)
                .mapToObj(id -> NewcomerView.builder().githubId(id).build().getCover())
                .toList();

        // Then
        assertThat(covers).doesNotContainNull();
        assertThat(covers).containsOnly(UserProfileCover.values());
        assertThat(covers.stream().sorted().distinct().toList().size()).isGreaterThan(1); // At least 2 different covers (randomness is not perfect)
    }
}