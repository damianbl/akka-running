package eu.softelo.akka

import eu.softelo.akka.TicketSeller.{Ticket, Tickets}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

case class Error(message: String)

trait EventMarshalling extends DefaultJsonProtocol {

  import BoxOffice._

  implicit val eventDescriptionFormat: RootJsonFormat[EventDescription] = jsonFormat1(EventDescription)
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat2(Event)
  implicit val eventsFormat: RootJsonFormat[Events] = jsonFormat1(Events)
  implicit val ticketRequestFormat: RootJsonFormat[TicketRequest] = jsonFormat1(TicketRequest)
  implicit val ticketFormat: RootJsonFormat[Ticket] = jsonFormat1(TicketSeller.Ticket)
  implicit val ticketsFormat: RootJsonFormat[Tickets] = jsonFormat2(TicketSeller.Tickets)
  implicit val errorFormat: RootJsonFormat[Error] = jsonFormat1(Error)
}
