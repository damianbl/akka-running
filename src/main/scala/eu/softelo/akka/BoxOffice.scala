package eu.softelo.akka

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import eu.softelo.akka.BoxOffice._

import scala.collection.immutable.Iterable
import scala.concurrent.Future

class BoxOffice(implicit timeout: Timeout) extends Actor {

  import context._

  def createTickerSeller(name: String): ActorRef = context.actorOf(TicketSeller.props(name), name)

  override def receive: Receive = {
    case CreateEvent(name, tickets) =>
      def create() = {
        val ticketSeller = createTickerSeller(name)

        val newTickets: Vector[TicketSeller.Ticket] = (1 to tickets) map { ticketId =>
          TicketSeller.Ticket(ticketId)
        } toVector

        ticketSeller ! TicketSeller.Add(newTickets)

        sender() ! EventCreated(Event(name, tickets))
      }

      context.child(name).fold(create())(_ => sender() ! EventExists)

    case GetTickets(event, tickets) =>
      def notFound() = sender() ! TicketSeller.Tickets(event)

      def buy(childActor: ActorRef) = childActor.forward(TicketSeller.Buy(tickets))

      context.child(event).fold(notFound())(buy)

    case GetEvent(event) =>
      def notFound() = sender() ! None

      def getEvent(child: ActorRef) = child forward TicketSeller.GetEvent

      context.child(event).fold(notFound())(getEvent)

    case GetEvents =>
      import akka.pattern.ask
      import akka.pattern.pipe

      def getEvents(): Iterable[Future[Option[Event]]] = context.children.map { child =>
        self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
      }

      def convertToEvents(f: Future[Iterable[Option[Event]]]) = f.map(_.flatten).map(l => Events(l.toVector))

      pipe(convertToEvents(Future.sequence(getEvents()))) to sender()

    case CancelEvent(event) =>
      def notFound() = sender() ! None

      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel

      context.child(event).fold(notFound())(cancelEvent)
  }
}

object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)

  def name = "boxOffice"

  case class CreateEvent(name: String, tickets: Int)

  case class GetEvent(name: String)

  case object GetEvents

  case class GetTickets(event: String, tickets: Int)

  case class CancelEvent(name: String)

  case class Event(name: String, tickets: Int)

  case class Events(events: Vector[Event])

  sealed trait EventResponse

  case class EventCreated(event: Event) extends EventResponse

  case object EventExists extends EventResponse

}
