package service

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class ConversationManager(
                           clientService: AWSService,
                           config: Config
                         )(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val maxTurns = config.getInt("conversation.max-turns")
  private var turnCount = 0

  def runConversation(initialQuery: String)(implicit system: ActorSystem[_]): Future[Unit] = {
    def conversationLoop(currentQuery: String): Future[Unit] = {
      if (turnCount >= maxTurns) {
        logger.info(s"Conversation ended after $maxTurns turns")
        Future.successful(())
      } else {
        for {
          // Call server with current query
          serverResponse <- clientService.callServer(currentQuery)
          _ = logger.info(s"Received server response: $serverResponse")

          // Process server response with Ollama
          ollamaResponse <- clientService.processWithOllama(serverResponse)
          _ = logger.info(s"Generated Ollama response: $ollamaResponse")

          // Increment turn count and continue conversation
          _ = turnCount += 1
          result <- conversationLoop(ollamaResponse)
        } yield result
      }
    }

    conversationLoop(initialQuery)
  }
}
