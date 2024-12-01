# LLM Conversational Agent Client

A Scala-based client implementation for interacting with the LLM Conversational Agent Server. This client application communicates with a server endpoint and integrates with Ollama for local LLM processing.

## Related Applications
- [LLM Conversational Agent Server](https://github.com/prat-bane/LLMConversationalAgent) - Server component that connects to AWS Bedrock

## Project Structure
```
src/
├── main/
│   ├── scala/
│   │   └── service/
│   │       ├── AWSService.scala            # AWS service interactions
│   │       ├── ConversationManager.scala   # Manages conversation flow
│   │       ├── OllamaService.scala         # Ollama integration
│   │       └── Main.scala                  # Application entry point
│   └── resources/
│       ├── application.conf                # Application configuration
│       ├── logback.xml                     # Logging configuration
│       └── conversation_log_*.txt          # Generated conversation logs
├── test/
│   └── scala/
│       └── service/
│           └── ConversationManagerSpec.scala  # Test specifications
└── docker/
    ├── Dockerfile                          # Docker image definition
    ├── docker-compose.yml                  # Multi-container Docker setup
    └── docker-compose-local.yml            # Local development setup
```

## Prerequisites
- Scala 2.13.10
- Amazon Corretto JDK 11
- SBT
- Docker and Docker Compose
- [Ollama](https://ollama.ai/) installed locally

## Configuration

Create `src/main/resources/application.conf`:
```hocon
server {
  host = "localhost"
  port = 8080
  endpoint = "api/v1/chat"
}

ollama {
  host = "http://localhost:11434"
  model = "llama2"
  request-timeout-seconds = 500
}

conversation {
  max-turns = 5
  initial-prompt = "Hello, how can I assist you today?"
}

aws {
  region = "us-east-1"
  endpoint = "your-server-endpoint"
}
```

## Building and Running

### Local Development
1. Build the project:
```bash
sbt clean compile
```

2. Run tests:
```bash
sbt test
```

3. Run the application:
```bash
sbt run
```

### Docker Deployment
1. Build the Docker image:
```bash
docker build -t llm-client .
```

2. Run with Docker Compose:
```bash
# For production setup
docker-compose up

# For local development
docker-compose -f docker-compose-local.yml up
```

## Usage

The client application provides several modes of operation:

1. Single Query Mode:
```bash
sbt "run --query 'Your question here'"
```

2. Interactive Mode:
```bash
sbt "run --interactive"
```

3. Batch Mode (from file):
```bash
sbt "run --file path/to/queries.txt"
```

## Conversation Logs

Conversation logs are automatically generated in `src/main/resources/` with the format:
```
conversation_log_YYYYMMDD_HHMMSS.txt
```

Example log format:
```
=== Conversation Log Started ===
Query: What is machine learning?
Bedrock Response: Machine learning is a branch of artificial intelligence...
Llama Response: Let me ask about specific applications...
----------------------------------------
```

## Testing
Run the test suite using:
```bash
sbt test
```

The tests include:
- Conversation flow testing
- Ollama integration testing
- Error handling scenarios
- Response formatting validation

## Docker Configuration

### Production Deployment
The `docker-compose.yml` file configures:
- Client application container
- Ollama service integration
- Network settings for server communication

### Local Development
The `docker-compose-local.yml` provides:
- Hot-reloading for development
- Local volume mounts
- Debug port mapping

## Troubleshooting

Common issues and solutions:

1. Ollama Connection:
   - Ensure Ollama is running locally
   - Check the Ollama host configuration
   - Verify model availability

2. Server Connection:
   - Verify server endpoint in configuration
   - Check network connectivity
   - Ensure proper port forwarding in Docker

3. Log Issues:
   - Check write permissions in resources directory
   - Verify logback configuration
   - Monitor disk space for logs

## Error Handling

The client implements comprehensive error handling for:
- Network connectivity issues
- Server timeouts
- Ollama processing errors
- Invalid responses
- Resource limitations




## License

This project is licensed under the MIT License - see the LICENSE file for details.
