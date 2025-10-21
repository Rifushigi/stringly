package com.rifushigi.stringly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParseResult(
        Boolean is_palindrome,
        Integer min_length,
        Integer max_length,
        Integer word_count,
        String contains_character
) {
}
