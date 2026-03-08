package dev.jeatog.comala.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jeatog.comala.persistencia.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarUsuarioReq(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password,
        @NotNull RolUsuario rol
) {}
