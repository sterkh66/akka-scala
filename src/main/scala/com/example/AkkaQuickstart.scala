//#full-example
package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }


case class ComplexMessage(name: String, ts: Int)

//#greeter-companion
//#greeter-messages
object Greeter {
  //#greeter-messages
  def props(message: String, printerActor: ActorRef, complexMessage: Option[ComplexMessage] = None): Props =
    Props(new Greeter(message, printerActor, complexMessage))
  //#greeter-messages
  final case class WhoToGreet(who: String)
  case object Greet
}
//#greeter-messages
//#greeter-companion

//#greeter-actor
class Greeter(message: String, printerActor: ActorRef, complexMessage: Option[ComplexMessage] = None ) extends Actor {
  import Greeter._
  import Printer._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = message + ", " + who
    case Greet           =>
      //#greeter-send-message
      printerActor ! Greeting(greeting)
      printerActor ! complexMessage.getOrElse("")
      //#greeter-send-message
    case ComplexMessage(name, ts) =>
      printerActor ! ComplexMessage(name, ts)
  }
}
//#greeter-actor

//#printer-companion
//#printer-messages
object Printer {
  //#printer-messages
  def props: Props = Props[Printer]
  //#printer-messages
  final case class Greeting(greeting: String)
}
//#printer-messages
//#printer-companion

//#printer-actor
class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from " + sender() + "): " + greeting)
    case ComplexMessage(name, ts) =>
      log.info("Event received (from " + sender() + "): Event occured name = " + name + ", ts = " + ts)
  }
}
//#printer-actor

//#main-class
object AkkaQuickstart extends App {
  import Greeter._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("helloAkka")

  //#create-actors
  // Create the printer actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  val complexMessage = ComplexMessage("event1", 1234567891)

  // Create the 'greeter' actors
  val howdyGreeter: ActorRef =
    system.actorOf(Greeter.props("Howdy", printer, Some(complexMessage)), "howdyGreeter")
  val helloGreeter: ActorRef =
    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter: ActorRef =
    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")
  //#create-actors

  //#main-send-messages
  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet
  howdyGreeter ! ComplexMessage("event2", 1234567892)

  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet
  howdyGreeter ! ComplexMessage("event3", 1234567893)

  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet
  helloGreeter ! ComplexMessage("event4", 1234567894)

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet
  //#main-send-messages
}
//#main-class
//#full-example
