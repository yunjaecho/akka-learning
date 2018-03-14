package com.lightbend.akka.sample



import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.lightbend.akka.sample.WebPageCollector.Collect
import com.lightbend.akka.sample.WordCounter.CountedWords
import com.lightbend.akka.sample.WordCuntMainActor.{Count, WebPageCountent}
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by USER on 2018-02-26.
  */
object WordCountApp extends App {
  val system = ActorSystem("wordCuntApp")

  val wordCuntMainActor = system.actorOf(WordCuntMainActor.props)

  wordCuntMainActor ! Count(List(
    "https://www.google.co.kr/search?q=akka",
    "https://www.google.co.kr/search?q=scala",
    "https://www.google.co.kr/search?q=play+framework"))

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

    case WebPageCountent(html) =>
      //println(s"WebPageContent $html")
      val doc = Jsoup.parse(html)
      val content = doc.body().text()
      val wordCounter = context.actorOf(WordCounter.props)
      wordCounter ! WordCounter.Count(content)

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


object WordCounter {
  sealed trait Command
  case class Count(content: String) extends Command

  sealed trait Result
  case class CountedWords(wordAndCount: Map[String, Int])

  def props = Props(new WordCounter)
}

class WordCounter extends Actor with ActorLogging {
  override def receive: Receive = {
    case WordCounter.Count(content) =>
      val wordCountingWorker = context.actorOf(WordCountingWorker.props)
      wordCountingWorker ! Count(content.split("[\\s]+").toList)
  }
}

/**
  *  words.groupBy(x => x) is same words.groupBy(identity)
  */
class WordCountingWorker extends Actor with ActorLogging {
  import scala.collection.mutable

  // 1. 외부에 노출시 데이터가 불변 이므로 변경불가능(안전)
  private var map = Map[String, Int]()

  // 2. 외부에 노출시 데이터가 불변이 아니므로 변경가능함(불불안)
  //private val map = mutable.Map[String, Int]()



  override def receive: Receive = {
    case Count(words) => {
      for(word <- words) {
        val count = map.get(word).fold(1)(_ +1)
        map += word -> count
      }

      sender() ! CountedWords(map)
      map = Map[String, Int]()


      //val wordCount = words.groupBy(x => x).mapValues(_.size)
      //println(wordCount)
      //println(words.groupBy(identity).map(t => (t._1, t._2.size)).toList)
      //println(words.groupBy(identity).map{case (word, list) => (word, list)}.toList)
    }
  }
}

object WordCountingWorker {
  sealed trait Command

  case class Count(words: List[String]) extends Command

  def props = Props(new WordCountingWorker())
}
