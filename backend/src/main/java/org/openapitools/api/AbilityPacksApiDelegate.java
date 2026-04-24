package org.openapitools.api;

import org.springframework.lang.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

/**
 * A delegate to be called by the {@link AbilityPacksApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public interface AbilityPacksApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * POST /ability-packs/import : Import ability pack
     *
     * @param file  (optional)
     * @return Ability pack imported (status code 200)
     * @see AbilityPacksApi#abilityPacksImportPost
     */
    default ResponseEntity<Void> abilityPacksImportPost(MultipartFile file) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
