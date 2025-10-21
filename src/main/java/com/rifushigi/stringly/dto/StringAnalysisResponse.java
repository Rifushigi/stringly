package com.rifushigi.stringly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

public record StringAnalysisResponse(
        String id,
        String value,
        Properties properties,

        @JsonProperty("created_at")
        LocalDateTime createdAt

        ) {
    public record Properties(
            Integer length,

            @JsonProperty("is_palindrome")
            Boolean isPalindrome,

            @JsonProperty("unique_characters")
            Integer uniqueCharacters,

            @JsonProperty("word_count")
            Integer wordCount,

            @JsonProperty("sha256_hash")
            String sha256Hash,

            @JsonProperty("character_frequency_map")
            Map<String, Integer> characterFrequencyMap
    ){}
}
