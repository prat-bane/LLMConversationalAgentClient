# LLM Conversational Agent Client

### Author : Pratyay Banerjee
### Email : pbane8@uic.edu
### Youtube video : [https://youtu.be/1tyuYzvIcBE](https://youtu.be/1tyuYzvIcBE)
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
http {
  interface = "0.0.0.1"
  port = 8081
}

server{
  url = ${?SERVER_URL}
  url = "http://localhost:8080" #for local without docker
 # url = "http://server:8080" for local with docker
  url = "http://ec2-34-226-246-115.compute-1.amazonaws.com:8080"
}

llm {
  maxResponseLength = 1000
  timeout = 30s
}

ollama {
  host = "http://host.docker.internal:11434" #will be http://localhost:11434 to run it locally without docker
  model = "llama3.2:1b"
  request-timeout-seconds = 500
  num-predict = 50
  temperature = 0.7
  top-k = 40
  top-p = 0.9

}

conversation {
  max-turns = 5  # Maximum number of back-and-forth turns in a conversation
  directory = "src/main/resources" # for local
  #directory = "/app/resources"  # for local with docker
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
sbt
> run "What is the weather today?"
```
Or set the argument in Intellij and run the Main class.
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

