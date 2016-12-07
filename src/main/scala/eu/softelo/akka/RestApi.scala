package eu.softelo.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor

class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContextExecutor = actorSystem.dispatcher

}

trait RestRoutes {
  def routes: Route = ???
}
