package onlydust.com.marketplace.project.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest {

    @ParameterizedTest
    @CsvSource({
            "foo, foo",
            "Foo, foo",
            "foo-bar, foo-bar",
            "foo Bar, foo-bar",
            "foo   bar, foo-bar",
            "foo    bar, foo-bar",
            "foo_bar, foo_bar",
            "foo_bar baz, foo_bar-baz",
            "foo_bar 0 qux, foo_bar-0-qux"
    })
    void slugOf(String name, String expectedSlug) {
        assertThat(Project.slugOf(name)).isEqualTo(expectedSlug);
    }
}