package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;

import java.util.List;

public record BroadcastDTO<RecipientData>(@NonNull @JsonProperty("per_user_data") List<RecipientDataDTO<RecipientData>> recipientDataDTOS) {

    public static BroadcastDTO<ProjectApplicationsToReviewByUserDTO> fromProjectApplicationsToReviewByUserDTO(
            final List<ProjectApplicationsToReviewByUser> projectApplicationsToReviewByUsers) {
        return new BroadcastDTO<>(projectApplicationsToReviewByUsers.stream()
                .map(event -> {
                    final ProjectApplicationsToReviewByUserDTO projectApplicationsToReviewByUserDTO = ProjectApplicationsToReviewByUserDTO.fromEvent(event);
                    return new RecipientDataDTO<>(event.getEmail(),
                            projectApplicationsToReviewByUserDTO);
                })
                .toList());
    }
}
