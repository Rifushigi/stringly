package com.rifushigi.stringly.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Data
public class StringAnalysis {

    private String id;

    private String value;

    private Integer length;

    @JsonProperty("is_palindrome")
    private Boolean isPalindrome;

    @JsonProperty("unique_characters")
    private Integer uniqueCharacters;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("sha256_hash")
    private String sha256Hash;

    @JsonProperty("character_frequency_map")
    private Map<String, Integer> characterFrequencyMap;

    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


}
