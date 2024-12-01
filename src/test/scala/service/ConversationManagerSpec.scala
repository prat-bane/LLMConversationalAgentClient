package service

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import java.nio.file.{Files, Path, Paths}

class ConversationManagerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterAll {
  private val testKit = ActorTestKit()
  implicit val system: ActorSystem[Nothing] = testKit.system
  implicit val ec: ExecutionContext = system.executionContext

  private val mockConfig = ConfigFactory.parseString(
    """
      |conversation {
      |  max-turns = 2
      |  directory = "src/test/resources"
      |}
      |""".stripMargin)

  private val mockAWSService = mock[AWSService]

  "ConversationManager" should {
    "successfully complete a conversation within max turns" in {
      // Setup
      when(mockAWSService.callServer(any[String])(any[ActorSystem[_]]))
        .thenReturn(Future.successful("Server Response"))
      when(mockAWSService.processWithOllama(any[String]))
        .thenReturn(Future.successful("Ollama Response"))

      val manager = new ConversationManager(mockAWSService, mockConfig)

      // Execute
      val result = manager.runConversation("Initial Query")

      // Verify
      whenReady(result, timeout(5.seconds)) { _ =>
        verify(mockAWSService, times(2)).callServer(any[String])(any[ActorSystem[_]])
        verify(mockAWSService, times(2)).processWithOllama(any[String])

        // Verify log file was created
        val timestamp = java.time.LocalDateTime.now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        val logFiles = Files.list(Paths.get("src/test/resources"))
          .filter(p => p.getFileName.toString.contains(timestamp))
          .findFirst()

        logFiles.isPresent shouldBe true
      }
    }
  }

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
    // Clean up test log files
    Files.list(Paths.get("src/test/resources"))
      .filter(p => p.getFileName.toString.startsWith("conversation_log"))
      .forEach(Files.delete)
  }
}
