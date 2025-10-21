package com.rifushigi.stringly.dto;

public record ParseResult(
        Boolean is_palindrome,
        Integer min_length,
        Integer max_length,
        Integer word_count,
        String contains_character
) {
}
