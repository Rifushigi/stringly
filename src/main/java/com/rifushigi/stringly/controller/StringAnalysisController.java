package com.rifushigi.stringly.controller;

import com.rifushigi.stringly.dto.*;
import com.rifushigi.stringly.entity.StringAnalysis;
import com.rifushigi.stringly.service.LLMService;
import com.rifushigi.stringly.service.StringAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/strings")
public class StringAnalysisController {

    private final StringAnalysisService sas;
    private final LLMService llm;

    @PostMapping
    public ResponseEntity<StringAnalysisResponse> analyseString(@Valid @RequestBody StringRequest request){
        StringAnalysis analysis = sas.analyseString(request.value());
        StringAnalysisResponse response = new StringAnalysisResponse(analysis);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{string_value}")
    public ResponseEntity<StringAnalysisResponse> getStringAnalysis(@PathVariable("string_value") String stringValue){
        Optional<StringAnalysis> analysis = sas.findByValue(stringValue);
        StringAnalysisResponse response = new StringAnalysisResponse(analysis.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<FilteredAnalysisResponse> getAllStrings(
            @RequestParam(value = "is_palindrome", required = false) Boolean isPalindrome,
            @RequestParam(value = "min_length", required = false) Integer minLength,
            @RequestParam(value = "max_length", required = false) Integer maxLength,
            @RequestParam(value = "word_count", required = false) Integer wordCount,
            @RequestParam(value = "contains_character", required = false) String containsCharacter
    ){
        List<StringAnalysis> results = sas.findWithFilters(isPalindrome, minLength, maxLength, wordCount, containsCharacter);
        List<StringAnalysisResponse> data = results.stream()
                .map(StringAnalysisResponse::new)
                .toList();
        ParseResult filtersApplied = new ParseResult(isPalindrome, minLength, maxLength, wordCount, containsCharacter, null);
        FilteredAnalysisResponse response = new FilteredAnalysisResponse(data, data.size(), filtersApplied);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter-by-natural-language")
    public ResponseEntity<NaturalLanguageFilterResponse> filterByNaturalLanguage(@RequestParam("query") String query) throws Exception {
        ParseResult parseResult = llm.parseQuery(query);
        List<StringAnalysis> result = sas.findWithFilters(
                parseResult.is_palindrome(),
                parseResult.min_length(),
                parseResult.max_length(),
                parseResult.word_count(),
                parseResult.contains_character()
        );
        List<String> data = result.stream().map(StringAnalysis::getValue).toList();
        Map<String, Object> interpretedQuery = new HashMap<>();
        interpretedQuery.put("original", query);
        interpretedQuery.put("parsed_filters", parseResult);
        NaturalLanguageFilterResponse response = new NaturalLanguageFilterResponse(data, data.size(), interpretedQuery);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/string_value")
    public ResponseEntity<Void> deleteString(@PathVariable("string_value") String stringValue){
        sas.deleteByValue(stringValue);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
