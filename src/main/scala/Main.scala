import org.slf4j.LoggerFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import service.{AWSService, ConversationManager}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}


object Main {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: ClientMain <initial-query>")
      System.exit(1)
    }

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "llm-client")
    implicit val executionContext: ExecutionContext = system.executionContext

    val config = ConfigFactory.systemEnvironment()          // Load env vars
      .withFallback(ConfigFactory.load())
    val initialQuery = args(0)

    val clientService = new AWSService(config)
    val conversationManager = new ConversationManager(clientService, config)

    logger.info(s"Starting conversation with initial query: $initialQuery")

    val conversationFuture = conversationManager.runConversation(initialQuery)

    conversationFuture.onComplete {
      case Success(_) =>
        logger.info("Conversation completed successfully")
        system.terminate()
      case Failure(ex) =>
        logger.error(s"Conversation failed: ${ex.getMessage}", ex)
        system.terminate()
    }

    Await.result(system.whenTerminated, 1.hour)
  }


}

