package onlydust.com.marketplace.project.domain.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectDetailsViewTest {
    @Test
    void should_return_true_when_is_member() {
        assertTrue(new ProjectDetailsView.Me(true, false, false, false).isMember());
        assertTrue(new ProjectDetailsView.Me(false, true, false, false).isMember());
        assertTrue(new ProjectDetailsView.Me(false, false, true, false).isMember());
        assertTrue(new ProjectDetailsView.Me(true, false, true, false).isMember());
        assertTrue(new ProjectDetailsView.Me(false, true, true, false).isMember());

        assertFalse(new ProjectDetailsView.Me(false, false, false, true).isMember());
        assertFalse(new ProjectDetailsView.Me(false, false, false, false).isMember());
    }
}