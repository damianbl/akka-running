package eu.softelo.akka

import akka.actor.{Actor, PoisonPill, Props}
import eu.softelo.akka.TicketSeller._

class TicketSeller(event: String) extends Actor {
  var tickets = Vector.empty[Ticket]

  override def receive: Receive = {
    case Add(newTickets: Vector[Ticket]) => tickets = tickets ++ newTickets

    case Buy(nrOfTickets: Int) =>
      val entries = tickets.take(nrOfTickets)
      if (entries.size >= nrOfTickets) {
        sender() ! Tickets(event, entries)
        tickets = tickets.drop(nrOfTickets)
      } else {
        sender() ! Tickets(event)
      }

    case GetEvent => sender() ! Some(BoxOffice.Event(event, tickets.size))

    case Cancel =>
      sender() ! Some(BoxOffice.Event(event, tickets.size))
      self ! PoisonPill
  }
}

object TicketSeller {
  def props(event: String) = Props(new TicketSeller(event))

  def name = "ticketSeller"

  case class Add(tickets: Vector[Ticket])

  case class Buy(tickets: Int)

  case class Ticket(id: Int)

  case class Tickets(event: String, entries: Vector[Ticket] = Vector.empty[Ticket])

  case object GetEvent

  case object Cancel

}
