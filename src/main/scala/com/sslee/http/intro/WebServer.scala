package com.sslee.http.intro

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Success

object WebServer {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("myFirstHttpServer")
    implicit val mat = ActorMaterializer()

    implicit val ec = system.dispatcher

    val route = path("hellow") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>hellow world akka-http</h>"))
      }
    }

    val bindFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

    StdIn.readLine()
    bindFuture.flatMap(serverBinding => serverBinding.unbind()).onComplete(_ => system.terminate())

  }
}