package com.packt.akka

import akka.actor.{ActorSystem, Props}
import akka.routing.{FromConfig, RandomGroup}
import com.packt.akka.Worker.Work

/**
  * Created by USER on 2018-02-20.
  */
object Random extends App {
  val system = ActorSystem("random-router")

  val routerPool = system.actorOf(FromConfig.props(Props[Worker]), "random-router-pool")

  routerPool ! Work()
  routerPool ! Work()
  routerPool ! Work()
  routerPool ! Work()

  Thread.sleep(100)

  println("======================================")

  system.actorOf(Props[Worker], "w1")
  system.actorOf(Props[Worker], "w2")
  system.actorOf(Props[Worker], "w3")

  val paths = List("/user/w1", "/user/w2", "/user/w3")

  val routerGroup = system.actorOf(RandomGroup(paths).props(), "random-router-group")

  routerGroup ! Work()
  routerGroup ! Work()
  routerGroup ! Work()
  routerGroup ! Work()


  Thread.sleep(100)

  system.terminate()
}
