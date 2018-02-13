import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Actor.Receive

import scala.sys.Prop

/**
  * Created by USER on 2018-02-12.
  */

case class WhoToGreet(who: String)

class Greeter extends Actor {
  override def receive: Receive = {
    case WhoToGreet(who) => println(s"Hello $who")
  }
}

object HelloAkka extends App{
  val system = ActorSystem("Hello-AKKA")
  val greeter = system.actorOf(Props[Greeter], "greeter")

  greeter ! WhoToGreet("Akka")
}
