package com.sslee.http.intro

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import akka.Done
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.server.Route
import scala.io.StdIn

object SimpleMarshallWebServer {

  case class Item(name: String, id: Long)
  case class Order(items: List[Item])

  var mockDatabase = List.empty[Item]

  //route 실행 필수
  implicit val system = ActorSystem("SimpleMarshallWebServer")
  implicit val mat = ActorMaterializer()

  //Future에 필요
  implicit val ec = system.dispatcher

  //fake select database
  def getItem(id: Long): Future[Option[Item]] = Future {
    mockDatabase.find(item => item.id == id)
  }

  //fake insert database
  def saveOrder(order: Order): Future[Done] = {
    order match {
      case Order(items) =>
        mockDatabase = items ::: mockDatabase
      case _ => mockDatabase
    }

    Future { Done }
  }

  def main(args: Array[String]): Unit = {

    implicit val itemFormatter = jsonFormat2(Item)
    implicit val orderFormatter = jsonFormat1(Order)

    val route: Route = get {
      pathPrefix("item" / LongNumber) { id =>
        val item = getItem(id)
        onSuccess(item) {
          case Some(item) => complete(item)
          case None => complete(StatusCodes.NotFound)
        }
      }
    } ~ post {
      path("createOrder") {
        entity(as[Order]) { order =>
          val saved: Future[Done] = saveOrder(order)
          onComplete(saved) { done =>
            complete("order created")
          }
        }
      }
    }

    val bindFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)
    StdIn.readLine()

    bindFuture.flatMap(serverBinding => serverBinding.unbind()).onComplete(_ => system.terminate())

  }

}