package com.rifushigi.stringly;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rifushigi.stringly.repository.StringAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StringAnalysisIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringAnalysisRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testPostString_Success() throws Exception {
        String value = "hello world";

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.value").value(value))
                .andExpect(jsonPath("$.properties.length").value(11))
                .andExpect(jsonPath("$.properties.is_palindrome").value(false))
                .andExpect(jsonPath("$.properties.unique_characters").value(8))
                .andExpect(jsonPath("$.properties.word_count").value(2))
                .andExpect(jsonPath("$.properties.sha256_hash").exists())
                .andExpect(jsonPath("$.properties.character_frequency_map.l").value(3))
                .andExpect(jsonPath("$.properties.character_frequency_map.o").value(2))
                .andExpect(jsonPath("$.properties.character_frequency_map[' ']").value(1))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    public void testPostString_PropertyCalculations_ExactMatch() throws Exception {
        // Test: length counts all characters including spaces and punctuation
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"a b!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.length").value(4));

        // Test: is_palindrome does NOT strip spaces (case-insensitive)
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"A man a plan a canal Panama\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.is_palindrome").value(false));

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"racecar\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.is_palindrome").value(true));

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"RaceCar\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.is_palindrome").value(true));

        // Test: unique_characters includes spaces
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"aa bb\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.unique_characters").value(3)); // 'a', 'b', ' '

        // Test: word_count uses split() - collapses multiple spaces, ignores leading/trailing
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"  hello   world  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.word_count").value(2));

        // Test: character_frequency_map includes spaces and punctuation
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"a, a!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.character_frequency_map.a").value(2))
                .andExpect(jsonPath("$.properties.character_frequency_map[',']").value(1))
                .andExpect(jsonPath("$.properties.character_frequency_map[' ']").value(1))
                .andExpect(jsonPath("$.properties.character_frequency_map['!']").value(1));
    }

    @Test
    public void testPostString_Duplicate_Returns409() throws Exception {
        String value = "duplicate test";

        // First POST - success
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated());

        // Second POST with same value - should return 409
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("String already exists in the system"));
    }

    @Test
    public void testPostString_MissingValue_Returns400Or422() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(anyOf(is(400), is(422))));
    }

    @Test
    public void testPostString_NonStringValue_Returns400Or422() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": 12345}"))
                .andExpect(status().is(anyOf(is(400), is(422))));

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": true}"))
                .andExpect(status().is(anyOf(is(400), is(422))));
    }

    @Test
    public void testPostString_InvalidJson_Returns400Or422() throws Exception {
        // Use a properly escaped string for invalid JSON test
        String invalidJson = "{\"incomplete\": ";
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().is(anyOf(is(400), is(422))));
    }

    @Test
    public void testGetStringByValue_Exists_Returns200() throws Exception {
        String value = "test string";

        // Create the string first
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated());

        // Retrieve it
        mockMvc.perform(get("/strings/{string_value}", value))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(value))
                .andExpect(jsonPath("$.properties").exists())
                .andExpect(jsonPath("$.properties.length").value(11))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    public void testGetStringByValue_UrlEncoded_Returns200() throws Exception {
        String value = "hello world";

        // Create the string
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated());

        // Retrieve with URL-encoded space (%20)
        mockMvc.perform(get("/strings/hello%20world"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(value));
    }

    @Test
    public void testGetStringByValue_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/strings/{string_value}", "nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("String does not exist in the system"));
    }

    @Test
    public void testGetAllStrings_NoFilters_Returns200() throws Exception {
        // Create multiple strings
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"world\"}"))
                .andExpect(status().isCreated());

        // Get all
        mockMvc.perform(get("/strings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data[0].value").exists())
                .andExpect(jsonPath("$.data[0].properties").exists());
    }

    @Test
    public void testGetAllStrings_FilterByPalindrome_Returns200() throws Exception {
        // Create palindrome and non-palindrome strings
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"racecar\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"level\"}"))
                .andExpect(status().isCreated());

        // Filter by palindrome=true
        mockMvc.perform(get("/strings")
                        .param("is_palindrome", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data[*].properties.is_palindrome", everyItem(is(true))));

        // Filter by palindrome=false
        mockMvc.perform(get("/strings")
                        .param("is_palindrome", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    public void testGetAllStrings_FilterByMinLength_Returns200() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hi\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello world\"}"))
                .andExpect(status().isCreated());

        // Filter by min_length=5
        mockMvc.perform(get("/strings")
                        .param("min_length", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].value").value("hello world"));
    }

    @Test
    public void testGetAllStrings_FilterByMaxLength_Returns200() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hi\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello world\"}"))
                .andExpect(status().isCreated());

        // Filter by max_length=5
        mockMvc.perform(get("/strings")
                        .param("max_length", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].value").value("hi"));
    }

    @Test
    public void testGetAllStrings_FilterByMinAndMaxLength_Returns200() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hi\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello world test\"}"))
                .andExpect(status().isCreated());

        // Filter by min_length=3 and max_length=10
        mockMvc.perform(get("/strings")
                        .param("min_length", "3")
                        .param("max_length", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].value").value("hello"));
    }

    @Test
    public void testGetAllStrings_FilterByWordCount_Returns200() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello world\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello world test\"}"))
                .andExpect(status().isCreated());

        // Filter by word_count=2
        mockMvc.perform(get("/strings")
                        .param("word_count", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].value").value("hello world"))
                .andExpect(jsonPath("$.data[0].properties.word_count").value(2));
    }

    @Test
    public void testGetAllStrings_FilterByContainsCharacter_Returns200() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"hello\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"zebra\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"Zoom\"}"))
                .andExpect(status().isCreated());

        // Filter by contains_character='z' (case-sensitive)
        mockMvc.perform(get("/strings")
                        .param("contains_character", "z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].value").value("zebra"));
    }

    @Test
    public void testGetAllStrings_MultipleFilters_Returns200() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"racecar\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"level\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"deed\"}"))
                .andExpect(status().isCreated());

        // Filter: is_palindrome=true AND min_length=5
        mockMvc.perform(get("/strings")
                        .param("is_palindrome", "true")
                        .param("min_length", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    public void testNaturalLanguageFilter_AllSingleWordPalindromicStrings() throws Exception {
        // Setup test data
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"racecar\"}"));
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"hello world\"}"));
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"level\"}"));

        mockMvc.perform(get("/strings/filter-by-natural-language")
                        .param("query", "all single word palindromic strings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.interpreted_query").exists());
    }

    @Test
    public void testNaturalLanguageFilter_StringsLongerThan10Characters() throws Exception {
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"short\"}"));
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"this is a longer string\"}"));

        mockMvc.perform(get("/strings/filter-by-natural-language")
                        .param("query", "strings longer than 10 characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.interpreted_query").exists());
    }

    @Test
    public void testNaturalLanguageFilter_StringsContainingLetterZ() throws Exception {
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"hello\"}"));
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"zebra\"}"));
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"amazing\"}"));

        mockMvc.perform(get("/strings/filter-by-natural-language")
                        .param("query", "strings containing the letter z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.interpreted_query").exists());
    }

    @Test
    public void testNaturalLanguageFilter_PalindromicStrings() throws Exception {
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"racecar\"}"));
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"hello\"}"));

        mockMvc.perform(get("/strings/filter-by-natural-language")
                        .param("query", "palindromic strings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.interpreted_query").exists());
    }

    @Test
    public void testDeleteString_Exists_Returns204() throws Exception {
        String value = "delete me";

        // Create the string
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated());

        // Delete it
        mockMvc.perform(delete("/strings/{string_value}", value))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/strings/{string_value}", value))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteString_UrlEncoded_Returns204() throws Exception {
        String value = "delete me test";

        // Create the string
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated());

        // Delete with URL-encoded value
        mockMvc.perform(delete("/strings/delete%20me%20test"))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/strings/delete%20me%20test"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteString_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/strings/{string_value}", "nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("String does not exists in the system"));
    }

    @Test
    public void testEmptyString() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.length").value(0))
                .andExpect(jsonPath("$.properties.word_count").value(0))
                .andExpect(jsonPath("$.properties.is_palindrome").value(true))
                .andExpect(jsonPath("$.properties.unique_characters").value(0));
    }

    @Test
    public void testStringWithOnlySpaces() throws Exception {
        mockMvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"   \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.length").value(3))
                .andExpect(jsonPath("$.properties.word_count").value(0))
                .andExpect(jsonPath("$.properties.unique_characters").value(1))
                .andExpect(jsonPath("$.properties.character_frequency_map[' ']").value(3));
    }

    @Test
    public void testJsonValidation() throws Exception {
        // Response must be valid JSON
        MvcResult result = mockMvc.perform(get("/strings"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        objectMapper.readTree(content); // Will throw if invalid JSON
    }

    @Test
    void testCreateAndRetrieveString() throws Exception {
        String value = "hello world";
        
        // Create the string
        mockMvc.perform(post("/strings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": \"" + value + "\"}"))
                .andExpect(status().isCreated());
        
        // Retrieve the string - properly encode the path variable
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        mockMvc.perform(get("/strings/" + encodedValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(value));
    }
}