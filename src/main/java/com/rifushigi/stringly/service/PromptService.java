package com.rifushigi.stringly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rifushigi.stringly.dto.ParseResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class PromptService implements LLMService{

    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String SYSTEM_PROMPT = """
        You are an assistant that extracts structured filters from natural language.
        Return ONLY a valid JSON object with these possible keys:
        - is_palindrome (boolean)
        - min_length (integer)
        - max_length (integer)
        - word_count (integer)
        - contains_character (single character string)

        Examples:
        'all single word palindromic strings' -> {"word_count": 1, "is_palindrome": true}
        'strings longer than 10 characters' -> {"min_length": 11}
        'palindromic strings that contain the letter a' -> {"is_palindrome": true, "contains_character": "a"}

        If none apply, return an empty JSON: {}
        """;


    public PromptService(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    @Override
    public ParseResult parseQuery(String query) throws Exception {
        Prompt prompt = Prompt.builder()
                .build()
                .augmentSystemMessage(SYSTEM_PROMPT)
                .augmentUserMessage(query);

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

        if (response == null){
            throw new Exception("Failed to parse query");
        }

        String json = response.getResult().getOutput().getText();

        try{
            return mapper.readValue(json, ParseResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
