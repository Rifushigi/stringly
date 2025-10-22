package com.rifushigi.stringly.repository;

import com.rifushigi.stringly.entity.StringAnalysis;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StringAnalysisRepository {

    private final Map<String, StringAnalysis> storage = new ConcurrentHashMap<>();

    public StringAnalysis save (StringAnalysis analysis) {
        storage.put(analysis.getId(), analysis);
        return analysis;
    }

    public Optional<StringAnalysis> findById(String id){
        return Optional.ofNullable(storage.get(id));
    }

    public List<StringAnalysis> findAll(){
        return storage.values().stream().toList();
    }

    public boolean existsById(String id){
        return storage.containsKey(id);
    }

    public void deleteById(String id){
        storage.remove(id);
    }

    public List<StringAnalysis> findWithFilters(Boolean isPalindrome, Integer minLength,
                                                Integer maxLength, Integer wordCount, String containsCharacter){
        return storage.values().stream()
                .filter(s -> isPalindrome == null || s.getIsPalindrome().equals(isPalindrome))
                .filter(s -> minLength == null || s.getLength() >= minLength)
                .filter(s -> maxLength == null || s.getLength() <= maxLength)
                .filter(s -> wordCount == null || s.getWordCount().equals(wordCount))
                .filter(s -> containsCharacter == null || s.getValue().toLowerCase().contains(containsCharacter.toLowerCase()))
                .toList();
    }

    public void deleteAll() {
        storage.clear();
    }
}
