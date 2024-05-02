package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeLanguageManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.project.domain.port.input.LanguageFacadePort;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@RestController
@Tags(@Tag(name = "BackofficeLanguageManagement"))
@AllArgsConstructor
public class BackofficeLanguageRestApi implements BackofficeLanguageManagementApi {
    private final LanguageFacadePort languageFacadePort;

    @Override
    public ResponseEntity<LanguageResponse> createLanguage(LanguageCreateRequest request) {
        final var language = languageFacadePort.createLanguage(request.getName(), new HashSet<>(request.getFileExtensions()));
        return ResponseEntity.ok(mapLanguageResponse(language));
    }

    @Override
    public ResponseEntity<LanguageListResponse> listLanguages() {
        final var languages = languageFacadePort.listLanguages();
        return ResponseEntity.ok(new LanguageListResponse()
                .languages(languages.stream().map(BackOfficeMapper::mapLanguageResponse).collect(Collectors.toList())));
    }

    @Override
    public ResponseEntity<LanguageResponse> updateLanguage(UUID languageId, LanguageUpdateRequest languageUpdateRequest) {
        final var language = languageFacadePort.updateLanguage(mapLanguageUpdateRequest(languageId, languageUpdateRequest));
        return ResponseEntity.ok(mapLanguageResponse(language));
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadLanguagePicture(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw badRequest("Error while reading image data", e);
        }

        final URL imageUrl = languageFacadePort.uploadPicture(imageInputStream);
        final var response = new UploadImageResponse().url(imageUrl.toString());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LanguageExtensionListResponse> getKnownExtensions() {
        final var extensions = languageFacadePort.getKnownExtensions();
        return ResponseEntity.ok(mapLanguageExtensionResponse(extensions));
    }
}
