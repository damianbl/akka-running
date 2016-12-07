package eu.softelo.akka

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future

trait RequestTimeout {

  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}

object Main extends App with RequestTimeout {
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  private val log = Logging(actorSystem.eventStream, "go-ticks")

  private val config = ConfigFactory.load()
  private val host = config.getString("http.host")
  private val port = config.getInt("http.port")

  private val api = new RestApi(actorSystem, requestTimeout(config)).routes

  private val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port) //Starts the HTTP server

  bindingFuture.map { serverBinding =>
    log.info(s"RestApi bound to ${serverBinding.localAddress} ")
  }.onFailure {
    case ex: Exception =>
      log.error(ex, s"Failed to bind to $host : $port!")
      actorSystem.terminate()
  }
}
