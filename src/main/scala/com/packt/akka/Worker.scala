package com.packt.akka

import akka.actor.Actor

import com.packt.akka.Worker.Work

/**
  * Created by USER on 2018-02-19.
  */
class Worker extends Actor {
  import Worker._

  override def receive: Receive = {
    case msg: Work =>
      println(s"I received Work Message and My ActorRef: ${self}")
  }
}

object Worker {
  case class Work()
}
