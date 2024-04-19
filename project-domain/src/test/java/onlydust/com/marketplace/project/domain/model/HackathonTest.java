package onlydust.com.marketplace.project.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HackathonTest {

    @ParameterizedTest
    @CsvSource({
            "foo,foo",
            "foo bar,foo-bar",
            "Foo Bar,foo-bar",
            "Foo Bar 123,foo-bar-123",
            "%(*& *&^ %$%%$ fo00$^^ 89 (*&,fo00-89",
    })
    void test_slug(String input, String expectedSlug) {
        var hackathon = new Hackathon(input, "subtitle", ZonedDateTime.now(), ZonedDateTime.now());
        var slug = hackathon.slug();
        assertThat(slug).isEqualTo(expectedSlug);
    }

    @Test
    void test_slug_cannot_be_empty() {
        assertThatThrownBy(() -> new Hackathon("", "subtitle", ZonedDateTime.now(), ZonedDateTime.now()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Title must contain at least one alphanumeric character");

        assertThatThrownBy(() -> new Hackathon("%(*& *&^ %$%%$ $^^ (*&", "subtitle", ZonedDateTime.now(), ZonedDateTime.now()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Title must contain at least one alphanumeric character");
    }
}