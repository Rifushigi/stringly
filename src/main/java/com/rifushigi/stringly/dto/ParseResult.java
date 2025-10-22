package com.rifushigi.stringly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParseResult(
        Boolean is_palindrome,
        Integer min_length,
        Integer max_length,
        Integer word_count,
        String contains_character,
        ErrorDetail error
) {
    public record ErrorDetail(
            Integer status,
            String message
    ){}
}
