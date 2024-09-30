package com.onlydust.customer.io.adapter.dto;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectApplicationAcceptedDTOTest {

    @Test
    void fromEvent() {
        final var dto = ProjectApplicationAcceptedDTO.fromEvent("recipientLogin", new ApplicationAccepted(
                new NotificationProject(ProjectId.random(), "slug", "name"),
                new NotificationIssue(123L, "http://foo.foo", "title", "repoName", "description")
        ), "production");

        assertThat(dto).isNotNull();
        assertThat(dto.issue().description()).isEqualTo("description");
    }

    @Test
    void fromEvent_issue_without_description() {
        final var dto = ProjectApplicationAcceptedDTO.fromEvent("recipientLogin", new ApplicationAccepted(
                new NotificationProject(ProjectId.random(), "slug", "name"),
                new NotificationIssue(123L, "http://foo.foo", "title", "repoName", null)
        ), "production");

        assertThat(dto).isNotNull();
        assertThat(dto.issue().description()).isNull();
    }
}