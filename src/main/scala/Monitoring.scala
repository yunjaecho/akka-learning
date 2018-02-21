import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}

/**
  * Created by USER on 2018-02-14.
  */
class Ares(atherna: ActorRef) extends Actor {
  override def preStart(): Unit = {
    context.watch(atherna)
  }

  override def postStop(): Unit = {
    println("Ares postStop...")
  }

  override def receive: Receive = {
    case Terminated(_) =>
      println(s"Ares received Terminated")
      context.stop(self)
  }
}

class Athena extends Actor {
  override def receive: Receive = {
    case msg =>
      println(s"Athena received $msg")
      context.stop(self)
  }
}


object Monitoring extends  App {
  val system = ActorSystem("monitoring")

  val athena = system.actorOf(Props[Athena], "athena")

  val ares = system.actorOf(Props(classOf[Ares], athena), "ares")

  athena ! "Hi"

  Thread.sleep(500)

  system.terminate()
}
