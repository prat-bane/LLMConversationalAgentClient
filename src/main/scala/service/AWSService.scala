package service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

case class ServerQuery(text: String)
case class ServerResponse(text: String)

object ClientJsonProtocol {
  implicit val serverQueryFormat: RootJsonFormat[ServerQuery] = jsonFormat1(ServerQuery)
  implicit val serverResponseFormat: RootJsonFormat[ServerResponse] = jsonFormat1(ServerResponse)
}

class AWSService(config: Config)(implicit ec: ExecutionContext) {
  import ClientJsonProtocol._

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val serverUrl = config.getString("server.url")
  private val ollamaService = new OllamaService(config)

  def callServer(query: String)(implicit system: ActorSystem[_]): Future[String] = {
    logger.info(s"Calling server with query: $query")

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = s"$serverUrl/api/v1/chat",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        ServerQuery(query).toJson.toString
      )
    )

    Http()(system)
      .singleRequest(request)
      .flatMap { response =>
        response.status match {
          case StatusCodes.OK =>
            Unmarshal(response.entity).to[String].map { body =>
              val serverResponse = body.parseJson.convertTo[ServerResponse]
              serverResponse.text
            }
          case _ =>
            Unmarshal(response.entity).to[String].flatMap { error =>
              Future.failed(new RuntimeException(s"Server request failed: $error"))
            }
        }
      }
  }

  def processWithOllama(serverResponse: String): Future[String] = {
    ollamaService.generateNextQuery(serverResponse)
  }

  def shutdown(): Unit = {
    logger.info("Shutting down Client Service")
    ollamaService.shutdown()
  }
}