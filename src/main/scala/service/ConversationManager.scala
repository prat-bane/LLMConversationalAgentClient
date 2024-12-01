package service

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class ConversationTurn(
                             query: String,
                             bedrockResponse: String,
                             ollamaResponse: String
                           )

class ConversationManager(
                           clientService: AWSService,
                           config: Config
                         )(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val maxTurns = config.getInt("conversation.max-turns")
  private var turnCount = 0

  private val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
  private val logFilePath = Paths.get(config.getString("conversation.directory"), s"conversation_log_$timestamp.txt")

  private def initializeLogFile(): Unit = {
    Files.createDirectories(logFilePath.getParent)
    if (!Files.exists(logFilePath)) {
      Files.createFile(logFilePath)
      writeToFile("=== Conversation Log ===\n\n")
    }
  }

  private def writeToFile(content: String): Unit = {
    Files.write(
      logFilePath,
      content.getBytes,
      StandardOpenOption.CREATE,
      StandardOpenOption.APPEND
    )
  }

  private def logConversationTurn(turn: ConversationTurn): Unit = {
    val formattedContent =
      s"""Turn ${turnCount + 1}:
         |
         |Query: ${turn.query}
         |
         |Bedrock Response:
         |${turn.bedrockResponse}
         |
         |Ollama Response:
         |${turn.ollamaResponse}
         |----------------------------------------
         |
         |""".stripMargin

    writeToFile(formattedContent)
  }

  def runConversation(initialQuery: String)(implicit system: ActorSystem[_]): Future[Unit] = {
    initializeLogFile()

    def conversationLoop(currentQuery: String): Future[Unit] = {
      if (turnCount >= maxTurns) {
        writeToFile(s"\nConversation ended after $maxTurns turns\n")
        logger.info(s"Conversation ended after $maxTurns turns")
        Future.successful(())
      } else {
        for {
          serverResponse <- clientService.callServer(currentQuery)
          _ = logger.info(s"Received server response: $serverResponse")

          ollamaResponse <- clientService.processWithOllama(serverResponse)
          _ = logger.info(s"Generated Ollama response: $ollamaResponse")

          turn = ConversationTurn(
            query = currentQuery,
            bedrockResponse = serverResponse,
            ollamaResponse = ollamaResponse
          )
          _ = logConversationTurn(turn)

          _ = turnCount += 1
          result <- conversationLoop(ollamaResponse)
        } yield result
      }
    }

    conversationLoop(initialQuery)
  }
}