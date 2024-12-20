package onlydust.com.marketplace.api.read.model;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import onlydust.com.marketplace.api.contract.model.LanguageWithPercentageResponse;

public record LanguageWithLineCount(UUID id,
                                   String name,
                                   String slug,
                                   String logoUrl,
                                   String bannerUrl,
                                   String transparentLogoUrl,
                                   String color,
                                   Integer lineCount) {

    public static List<LanguageWithPercentageResponse> toLanguageWithPercentageResponse(List<LanguageWithLineCount> languages) {
        if (isNull(languages)) {
            return List.of();
        }

        final long totalLines = languages.stream()
                .mapToLong(language -> language.lineCount)
                .sum();

        return totalLines == 0L ? List.of() : languages.stream()
                .map(language -> new LanguageWithPercentageResponse()
                        .name(language.name)
                        .slug(language.slug)
                        .id(language.id)
                        .logoUrl(language.logoUrl)
                        .transparentLogoUrl(language.transparentLogoUrl)
                        .bannerUrl(language.bannerUrl)
                        .color(language.color)
                        .percentage(BigDecimal.valueOf(language.lineCount)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(totalLines), 2, java.math.RoundingMode.HALF_UP)))
                .sorted(comparing(LanguageWithPercentageResponse::getPercentage).reversed())
                .toList();
    }
}
