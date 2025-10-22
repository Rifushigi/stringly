package com.rifushigi.stringly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record NaturalLanguageFilterResponse (
        List<String> data,
        Integer count,
        @JsonProperty("interpreted_query")
        Map<String, Object> parsedQuery
) {
}
