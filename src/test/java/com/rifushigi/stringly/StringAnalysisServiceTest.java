package com.rifushigi.stringly;

import com.rifushigi.stringly.entity.StringAnalysis;
import com.rifushigi.stringly.exception.StringAlreadyExistsException;
import com.rifushigi.stringly.exception.StringNotFoundException;
import com.rifushigi.stringly.repository.StringAnalysisRepository;
import com.rifushigi.stringly.service.StringAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StringAnalysisServiceTest {

    @Mock
    private StringAnalysisRepository repository;

    @InjectMocks
    private StringAnalysisService service;

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    @Test
    void testAnalyseString_CorrectCalculations() {
        String value = "hello world";

        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StringAnalysis result = service.analyseString(value);

        assertEquals(value, result.getValue());
        assertEquals(11, result.getLength()); // including space
        assertFalse(result.getIsPalindrome());
        assertEquals(8, result.getUniqueCharacters()); // h,e,l,o, ,w,r,d
        assertEquals(2, result.getWordCount());
        assertNotNull(result.getSha256Hash());

        assertEquals(3, result.getCharacterFrequencyMap().get("l"));
        assertEquals(2, result.getCharacterFrequencyMap().get("o"));
        assertEquals(1, result.getCharacterFrequencyMap().get(" "));
    }

    @Test
    void testAnalyseString_Palindrome_DoesNotStripSpaces() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // "a b a" is NOT a palindrome when spaces are included
        StringAnalysis result1 = service.analyseString("a b a");
        assertTrue(result1.getIsPalindrome());

        // "racecar" IS a palindrome
        StringAnalysis result2 = service.analyseString("racecar");
        assertTrue(result2.getIsPalindrome());

        // Case-insensitive
        StringAnalysis result3 = service.analyseString("RaceCar");
        assertTrue(result3.getIsPalindrome());
    }

    @Test
    void testAnalyseString_CharacterFrequencyIncludesSpaces() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StringAnalysis result = service.analyseString("a a b");

        assertEquals(2, result.getCharacterFrequencyMap().get("a"));
        assertEquals(1, result.getCharacterFrequencyMap().get("b"));
        assertEquals(2, result.getCharacterFrequencyMap().get(" "));
    }

    @Test
    void testAnalyseString_Duplicate_ThrowsException() {
        String value = "duplicate";
        StringAnalysis existing = new StringAnalysis();

        when(repository.findById(any())).thenReturn(Optional.of(existing));

        assertThrows(StringAlreadyExistsException.class, () -> service.analyseString(value));

        verify(repository, never()).save(any());
    }

    @Test
    void testFindByValue_NotFound_ThrowsException() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(StringNotFoundException.class, () -> service.findByValue("nonexistent"));
    }

    @Test
    void testDeleteByValue_NotFound_ThrowsException() {
        when(repository.existsById(any())).thenReturn(false);

        assertThrows(StringNotFoundException.class, () -> service.deleteByValue("nonexistent"));

        verify(repository, never()).deleteById(any());
    }
}
