package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.UserProfileCover;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChurnedContributorViewTest {
    @Test
    void should_get_random_cover_by_default() {
        // Given a looooooot of covers
        final var covers = LongStream.rangeClosed(1, UserProfileCover.values().length * 10L)
                .mapToObj(id -> ChurnedContributorView.builder().githubId(id).build().getCover())
                .toList();

        // Then
        assertThat(covers).doesNotContainNull();
        assertThat(covers).containsOnly(UserProfileCover.values());
        assertThat(covers.stream().sorted().distinct().toList().size()).isEqualTo(UserProfileCover.values().length);
    }
}