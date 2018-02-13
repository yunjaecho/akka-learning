import MusicController._
import MusicPlayer._
import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Actor.Receive

/**
  * Created by USER on 2018-02-12.
  */
object MusicController {
  sealed trait ControllerMsg
  case object Play extends ControllerMsg
  case object Stop extends ControllerMsg

  def props = Props[MusicController]
}

class MusicController extends Actor {
  override def receive: Receive = {
    case Play => println("Music Started...")
    case Stop => println("Music Stopped...")
  }
}

object MusicPlayer {
  sealed trait PlayMsg
  case object StopMusic extends PlayMsg
  case object StartMusic extends PlayMsg
}

class MusicPlayer extends Actor {
  override def receive: Receive = {
    case StopMusic => println("I don't want to stop music")
    case StartMusic =>
      val controller = context.actorOf(props, "controller")
      controller ! Play
    case _ => println("Unknow Message")
  }
}


object MusicToActor extends App {
  val system = ActorSystem("creation")

  val player = system.actorOf(Props[MusicPlayer], "player")

  player ! StartMusic
}

