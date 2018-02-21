package com.packt.akka

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props, Stash}
import com.packt.akka.UserStorage.{Connect, DBOperation, DisConnect, Operation}


case class User(username: String, email: String)

object UserStorage {
  trait DBOperation
  object DBOperation {
    case object Create extends DBOperation
    case object Update extends DBOperation
    case object Read extends DBOperation
    case object Delete extends DBOperation
  }

  case object Connect
  case object DisConnect
  case class Operation(dBOperation: DBOperation, user: Option[User])
}

class UserStorage extends Actor with Stash {

  def receive = disconnected


  def connected: Actor.Receive = {
    case DisConnect =>
      println("User Storage Disconnected to DB")
      unstashAll()
      context.unbecome()
    case Operation(op, user) =>
      println(s"User Storage received ${op} to do in user: ${user}")
  }
  def disconnected: Actor.Receive = {
    case Connect =>
      println(s"User Storage connected to DB")
      context.become(connected)
    case _ =>
      println(s"Other..............")
      stash()
  }
}

object BecomeHotswap extends App {
  val system = ActorSystem("Hotswap-Become")

  val userStorage = system.actorOf(Props[UserStorage], "userStorage")

  userStorage ! Connect

  userStorage ! Operation(DBOperation.Create, Some(User("Admin", "admin@pact.com")))

  userStorage ! DisConnect

  Thread.sleep(100)

  system.terminate()
}
