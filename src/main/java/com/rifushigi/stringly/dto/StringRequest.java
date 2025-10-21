package com.rifushigi.stringly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StringRequest(@NotNull @NotBlank String value) {}
