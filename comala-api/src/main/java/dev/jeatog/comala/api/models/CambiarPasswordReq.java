package dev.jeatog.comala.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordReq(
        @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String passwordActual,
        @NotBlank @Size(min = 8) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String nuevaPassword
) {}
