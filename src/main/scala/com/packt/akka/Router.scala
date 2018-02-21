package com.packt.akka

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.packt.akka.Worker.Work

/**
  * Created by USER on 2018-02-19.
  */
class Router extends Actor {
  var routees: List[ActorRef] = _

  override def preStart(): Unit = {
    routees = List.fill(5)(context.actorOf(Props[Worker]))
  }

  override def receive: Receive = {
    case msg: Work =>
      println("I'm A Router and I received a Message...")
      routees(util.Random.nextInt(routees.size)).forward(msg)
  }
}

class RouterGroup(routees: List[String]) extends Actor {
  override def receive: Receive = {
    case msg: Work =>
      println(s"I'm a Router Group and I received Work Message......")
      context.actorSelection(routees(util.Random.nextInt(routees.size))).forward(msg)
  }
}


object Router extends App {
/*  println("router..............")

  val system = ActorSystem("router")

  val router = system.actorOf(Props[Router], "router")

  router ! Work()

  router ! Work()

  router ! Work()

  Thread.sleep(1000)

  system.terminate()*/

  val system = ActorSystem("router")

  system.actorOf(Props[Worker], "w1")
  system.actorOf(Props[Worker], "w2")
  system.actorOf(Props[Worker], "w3")
  system.actorOf(Props[Worker], "w4")
  system.actorOf(Props[Worker], "w5")

  val workers: List[String] = List("/user/w1", "/user/w2", "/user/w3", "/user/w4", "/user/w5")
  val routerGroup = system.actorOf(Props(classOf[RouterGroup], workers))

  routerGroup ! Work()

  routerGroup ! Work()

  Thread.sleep(100)

  system.terminate()
}