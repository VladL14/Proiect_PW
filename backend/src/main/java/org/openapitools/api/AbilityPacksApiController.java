package org.openapitools.api;

import org.springframework.lang.Nullable;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
@Controller
@RequestMapping("${openapi.diceDuel.base-path:/api}")
public class AbilityPacksApiController implements AbilityPacksApi {

    private final AbilityPacksApiDelegate delegate;

    public AbilityPacksApiController(@Autowired(required = false) AbilityPacksApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new AbilityPacksApiDelegate() {});
    }

    @Override
    public AbilityPacksApiDelegate getDelegate() {
        return delegate;
    }

}
