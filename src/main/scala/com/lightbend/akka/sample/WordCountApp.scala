package com.lightbend.akka.sample



import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.lightbend.akka.sample.WebPageCollector.Collect
import com.lightbend.akka.sample.WordCuntMainActor.{Count, WebPageCountent}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by USER on 2018-02-26.
  */
object WordCountApp extends App {
  val system = ActorSystem("wordCuntApp")

  val wordCuntMainActor = system.actorOf(WordCuntMainActor.props)

  wordCuntMainActor ! Count(List("https://www.naver.com", "https://www.daum.net"))

}

class WordCuntMainActor extends Actor with ActorLogging {
  implicit val system = context.system
  //implicit val materializer = ActorMaterializer()
  implicit val executionContext = context.system.dispatcher

  override def receive: Receive =  {
    case Count(urls) =>
      // create WebPageCollector. It is not WebPageCollector type but ActorRef type.
      //context.actorOf(Props(classOf[WebPageCollector], Http())) // 1번째 방법
      val webPageCollector: ActorRef = context.actorOf(WebPageCollector.props(Http())) // 2번째 방법 (권장 방법) , return Type "ActorRef"
      urls.foreach(url => webPageCollector ! Collect(url))

    case WebPageCountent(content) =>
      println(content)
  }
}

object WordCuntMainActor {
  sealed trait Command
  case class Count(urls: List[String]) extends Command

  sealed trait Result
  case class WebPageCountent(content: String) extends Result

  def props: Props = Props(new WordCuntMainActor())
}

class WebPageCollector(http: HttpExt)(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case Collect(url) =>
      println(s"$url - $sender")

      val theSender = sender
      val resonseFuture: Future[HttpResponse] = http.singleRequest(HttpRequest(uri = url))

      resonseFuture
        .onComplete {
          case Success(res) =>
            res.entity.dataBytes
               .runFold(ByteString(""))(_ ++ _)
               .map(body => body.utf8String)
               .onComplete({
                 case Success(r) =>
                   println(s"$url - $theSender")
                   theSender ! WebPageCountent(r)
                 case Failure(exception) =>
                   println(s"failed $exception")
               })
          case Failure(_) =>sys.error("something wrong")
        }
  }
}

object WebPageCollector {
  sealed trait Command
  case class Collect(url: String) extends Command

  def props(http: HttpExt)(implicit ec: ExecutionContext): Props = Props(new WebPageCollector(http))
}

class WordCounter extends Actor with ActorLogging {
  override def receive: Receive = {
    case Count(content) => ???
  }
}
