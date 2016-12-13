package eu.softelo.akka

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import eu.softelo.akka.BoxOffice.{Events, GetEvents}

import scala.concurrent.ExecutionContextExecutor

class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def createBoxOffice(): ActorRef = actorSystem.actorOf(BoxOffice.props, BoxOffice.name)
}

trait RestRoutes extends BoxOfficeApi with EventMarshalling {

  import StatusCodes._

  def routes: Route = eventsRoute

  def eventsRoute: Route =
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          // GET /events
          onSuccess(getEvents()) { events =>
            complete(OK, events)
          }
        }
      }
    }
}

trait BoxOfficeApi {
  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  lazy val boxOffice: ActorRef = createBoxOffice()

  def getEvents(): Future[Events] = boxOffice.ask(GetEvents).mapTo[Events]
}
