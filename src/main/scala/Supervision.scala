import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }

/**
  * Created by USER on 2018-02-13.
  */

class Aphrodite extends Actor {
  import Aphrodite._

  override def preStart(): Unit = {
    println("Aphrodite preStart hook")
  }

  override def postRestart(reason: Throwable) = {
    println("Aphrodite postRestart hook")
    super.postRestart(reason)
  }

  override def postStop() = {
    println("Aphrodite postStop hook")
  }


  override def receive: Receive = {
    case "Resume" => throw ResumeException
    case "Stop" => throw StopException
    case "Restart" => throw RestartException
    case _ => throw Exception
  }
}

object Aphrodite {
  case object ResumeException extends Exception
  case object StopException extends Exception
  case object RestartException extends Exception
}

object Supervision {

}
