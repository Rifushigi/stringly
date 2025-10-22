# Stringly - String Analysis API

A RESTful API service for analyzing and managing string data with advanced filtering capabilities, including natural language query processing powered by LLM integration.

## Features

- **String Analysis**: Comprehensive analysis of input strings including:
  - Length calculation
  - Palindrome detection (case-insensitive)
  - Unique character counting
  - Word count
  - SHA-256 hash generation
  - Character frequency mapping

- **Storage**: Persistent storage of analyzed strings using SHA-256 hash as unique identifier

- **Filtering**: Multiple filtering options:
  - Standard filters (palindrome, length range, word count, character containment)
  - Natural language query processing via LLM integration

- **Data Management**: Full CRUD operations for string analysis records

## Tech Stack

- **Framework**: Spring Boot
- **Language**: Java 25
- **ORM**: Jakarta EE with JPA
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Utilities**: Lombok

## Prerequisites

- Java 25 or higher
- Maven 3.6+

## Getting Started

### Installation

1. Clone the repository:
```
bash
git clone <repository-url>
cd stringly
```
2. Build the project:
```
bash
./mvnw clean install
```
3. Run the application:
```
bash
./mvnw spring-boot:run
```
The application will start on the default port (typically 8080).

## API Endpoints

### 1. Analyze String

Analyzes a new string and stores the results.

**POST** `/strings`

**Request Body:**
```
json
{
"value": "hello world"
}
```
**Response:** `201 Created`
```json
{
  "id": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
  "value": "hello world",
  "length": 11,
  "is_palindrome": false,
  "unique_characters": 8,
  "word_count": 2,
  "sha256_hash": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
  "character_frequency_map": {
    "h": 1,
    "e": 1,
    "l": 3,
    "o": 2,
    " ": 1,
    "w": 1,
    "r": 1,
    "d": 1
  },
  "created_at": "2025-10-22T10:30:00"
}
```
```


### 2. Get String Analysis

Retrieves analysis for a specific string.

**GET** `/strings/{string_value}`

**Example:** `GET /strings/hello%20world`

**Response:** `200 OK`

### 3. Filter Strings

Retrieves strings with optional filtering criteria.

**GET** `/strings?is_palindrome={boolean}&min_length={int}&max_length={int}&word_count={int}&contains_character={char}`

**Query Parameters:**
- `is_palindrome` (optional): Filter by palindrome status
- `min_length` (optional): Minimum string length
- `max_length` (optional): Maximum string length
- `word_count` (optional): Exact word count
- `contains_character` (optional): Filter strings containing specific character

**Example:** `GET /strings?is_palindrome=true&min_length=5`

**Response:** `200 OK`
```json
{
  "data": [...],
  "count": 10,
  "filters_applied": {
    "is_palindrome": true,
    "min_length": 5,
    "max_length": null,
    "word_count": null,
    "contains_character": null
  }
}
```


### 4. Natural Language Filter

Filter strings using natural language queries.

**GET** `/strings/filter-by-natural-language?query={natural_language_query}`

**Example:** `GET /strings/filter-by-natural-language?query=show me palindromes longer than 5 characters`

**Response:** `200 OK`
```json
{
  "data": ["racecar", "deified"],
  "count": 2,
  "interpreted_query": {
    "original": "show me palindromes longer than 5 characters",
    "parsed_filters": {
      "is_palindrome": true,
      "min_length": 6,
      "max_length": null,
      "word_count": null,
      "contains_character": null
    }
  }
}
```


### 5. Delete String

Deletes a string analysis record.

**DELETE** `/strings/{string_value}`

**Response:** `204 No Content`

## Error Responses

The API returns appropriate HTTP status codes and error messages:

- `400 Bad Request`: Invalid input or query parameters
- `404 Not Found`: String not found in system
- `409 Conflict`: String already exists in system
- `500 Internal Server Error`: Server-side error

**Error Response Format:**
```json
{
  "error": "String already exists in the system",
  "status": 409,
  "timestamp": "2025-10-22T10:30:00"
}
```


## Project Structure

```
src/main/java/com/rifushigi/stringly/
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions and global error handler
├── repository/          # Data access layer
├── service/             # Business logic
└── StringlyApplication  # Main application class
```


## Running Tests

Execute the test suite:

```shell script
./mvnw test
```


## Configuration

Application properties can be configured in:
- `src/main/resources/application.properties` - Default configuration
- `src/main/resources/application-dev.properties` - Development profile

## Key Features Explained

### Palindrome Detection
- Case-insensitive comparison
- Spaces and special characters are included in the check
- Example: "RaceCar" → palindrome, "race car" → not a palindrome

### Character Frequency Map
- Counts all characters including spaces
- Returns a map of character to frequency
- Example: "hello" → {"h": 1, "e": 1, "l": 2, "o": 1}

### SHA-256 Hash
- Used as unique identifier for each string
- Prevents duplicate string entries
- Ensures data integrity

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

[Specify your license here]
```
