package com.rifushigi.stringly.service;

import com.rifushigi.stringly.entity.StringAnalysis;
import com.rifushigi.stringly.exception.StringAlreadyExistsException;
import com.rifushigi.stringly.repository.StringAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StringAnalysisService {

    private StringAnalysisRepository repository;

    public StringAnalysis analyseString(String value){
        String sha256Hash = computeSha256Hash(value);

        Optional<StringAnalysis> existing = repository.findById(sha256Hash);
        if(existing.isPresent()){
            throw new StringAlreadyExistsException("String already exists in the system");
        }

        Integer length = computeLength(value);
        Boolean isPalindrome = computeIsPalindrome(value);
        Integer uniqueCharacters = computeUniqueCharacters(value);
        Integer wordCount = computeWordCount(value);
        Map<String, Integer> characterFrequencyMap = computeCharacterFrequencyMap(value);

        StringAnalysis analysis = new StringAnalysis(sha256Hash, value, length, isPalindrome,
                uniqueCharacters, wordCount, sha256Hash, characterFrequencyMap, LocalDateTime.now());

        return repository.save(analysis);
    }

    public Optional <StringAnalysis> findByValue(String value){
        String hash = computeSha256Hash(value);
        return repository.findById(hash);
    }

    public List<StringAnalysis> findWithFilters(Boolean isPalindrome, Integer minLength, Integer maxLength,
                                                Integer wordCount, String containsCharacter){
        return repository.findWithFilters(isPalindrome, minLength, maxLength, wordCount, containsCharacter);
    }

    public List<StringAnalysis> findAll(){
        return repository.findAll();
    }

    public Boolean deleteByValue(String value){
        String hash = computeSha256Hash(value);
        if (repository.existsById(hash)){
            repository.deleteById(hash);
            return true;
        }
        return false;
    }

    private Integer computeLength(String value){
        return value.length();
    }

    private Boolean computeIsPalindrome(String value){
        String cleaned = value.toLowerCase().replaceAll("\\s+", "");
        String reversed = new StringBuilder(cleaned).reverse().toString();
        return cleaned.equals(reversed);
    }

    private Integer computeUniqueCharacters(String value){
        return (int) value.chars().distinct().count();
    }

    private Integer computeWordCount(String value){
        return value.trim().split("\\s+").length;
    }

    private String computeSha256Hash(String value){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for(byte b : hash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length()==1){
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found", e);
        }
    }

    private Map<String, Integer> computeCharacterFrequencyMap(String value){
        Map<String, Integer> frequencyMap = new HashMap<>();
        for(char c : value.toCharArray()) {
            String key = String.valueOf(c);
            frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1);
        }
        return frequencyMap;
    }
}
