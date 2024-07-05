package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;

import java.util.List;

public record ProjectApplicationsToReviewByUserDTO(@NonNull String username,
                                                   @NonNull List<Project> projects) {

    public static ProjectApplicationsToReviewByUserDTO fromEvent(final ProjectApplicationsToReviewByUser event) {
        return new ProjectApplicationsToReviewByUserDTO(
                event.getUserLogin(),
                event.getProjects().stream()
                        .map(project -> new Project(
                                project.slug(),
                                project.name(),
                                project.issues().stream()
                                        .map(issue -> new Issue(
                                                issue.id(),
                                                issue.title(),
                                                issue.repoName(),
                                                issue.applicantCount()))
                                        .toList()))
                        .toList());
    }


    public record Project(@NonNull String slug,
                          @NonNull String name,
                          @NonNull List<Issue> issues) {
    }

    public record Issue(@NonNull Long id,
                        @NonNull String title,
                        @NonNull String repoName,
                        @NonNull Integer applicantCount) {
    }
}
