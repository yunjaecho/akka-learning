package com.packt.akka

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by USER on 2018-02-14.
  */
class Counter extends Actor {
  import Counter._

  var count = 0

  override def receive: Receive = {
    case Inc(x) => count += x
    case Dec(x) => count -= x
  }

}


object Counter {
  final case class Inc(num: Int)
  final case class Dec(num: Int)
}
