package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;

public record IssueDTO(
        @NonNull String title,
        String description,
        @NonNull String repository,
        @NonNull String detailsUrl
) {
}
