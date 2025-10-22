package com.rifushigi.stringly.service;

import com.rifushigi.stringly.dto.ParseResult;

public interface LLMService {
    /**
    * Parse a natural language query into filter parameters
    * @param query The natural language query
    * @return Map of filter parameters or null if parsing fails
    * @throws Exception if the LLM service is unavailable or returns an message
    **/
    ParseResult parseQuery(String query) throws Exception;
}
