package com.rifushigi.stringly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FilteredAnalysisResponse(
        List<StringAnalysisResponse> data,
        String count,
        @JsonProperty(value = "filters_applied")
        ParseResult filtersApplied
) {
}
