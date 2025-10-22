package com.rifushigi.stringly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rifushigi.stringly.entity.StringAnalysis;

import java.util.List;
import java.util.Map;

public record FilteredAnalysisResponse(
        List<StringAnalysis> data,
        String count,
        @JsonProperty(value = "filters_applied")
        ParseResult filtersApplied
) {
}
