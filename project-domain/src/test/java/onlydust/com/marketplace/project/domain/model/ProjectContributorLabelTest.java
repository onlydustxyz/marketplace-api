package onlydust.com.marketplace.project.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectContributorLabelTest {

    @ParameterizedTest
    @CsvSource({
            "foo,foo",
            "foo bar,foo-bar",
            "Foo Bar,foo-bar",
            "Foo Bar 123,foo-bar-123",
            "%(*& *&^ %$%%$ fo00$^^ 89 (*&,fo00-89",
    })
    void test_slug(String input, String expectedSlug) {
        var projectContributorLabel = new ProjectContributorLabel(ProjectContributorLabel.Id.random(), ProjectId.random(), input);
        assertThat(projectContributorLabel.slug()).isEqualTo(expectedSlug);


        var projectContributorLabel2 = new ProjectContributorLabel(ProjectContributorLabel.Id.random(), ProjectId.random(), "foo");
        projectContributorLabel2.name(input);
        assertThat(projectContributorLabel2.slug()).isEqualTo(expectedSlug);
    }

    @Test
    void test_slug_cannot_be_empty() {
        assertThatThrownBy(() -> new ProjectContributorLabel(ProjectContributorLabel.Id.random(), ProjectId.random(), ""))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Label must contain at least one alphanumeric character");

        assertThatThrownBy(() -> new ProjectContributorLabel(ProjectContributorLabel.Id.random(), ProjectId.random(), "%(*& *&^ %$%%$ $^^ (*&"))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Label must contain at least one alphanumeric character");

        final var projectContributorLabel = new ProjectContributorLabel(ProjectContributorLabel.Id.random(), ProjectId.random(), "foo");
        assertThatThrownBy(() -> projectContributorLabel.name(""))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Label must contain at least one alphanumeric character");

        assertThatThrownBy(() -> projectContributorLabel.name("%(*& *&^ %$%%$ $^^ (*&"))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Label must contain at least one alphanumeric character");
    }
}