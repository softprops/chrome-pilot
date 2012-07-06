package chrome

import dispatch._
import tubesocks._
import java.util.concurrent.CountDownLatch

import net.liftweb.json.JArray

import unfiltered.netty.{ Http => NHttp, _ }
import unfiltered.netty.cycle.Planify
import unfiltered.request._
import unfiltered.response._

object Server {
  def main(args: Array[String]) {
    args.toList match {
      case List(uri) =>
        @volatile var lat: Option[CountDownLatch] = None
        Http(url(uri) OK Json.parsed).either.right.map { js =>
          lat = js match {
            case JArray(ary) =>
              Some(new CountDownLatch(ary.size))
            case _ => 
              throw new RuntimeException("expected json array from chrome")
          }
          TabInfo.fromJson(js)
                  .filter(!_.title.startsWith("chrome-extension:"))
                  .map { info =>              
                    Tab(info, Channel.uri(info.wsdebugUrl) {
                      case Open(s) =>
                        println("[%s] open" format info.title)
                      case Close(s) =>
                        println("[%s] closed" format info.title)
                        lat.map(_.countDown())
                      case Message(m) =>
                        println("[%s] %s" format(info.title, m))
                    })
                 }
          }().fold(identity, { tabs =>
            println("communicating with %d chrome tabs" format tabs.size)          
            NHttp.anylocal.handler(Planify{
              case Path(Seg("tldr" :: Nil)) =>
                ResponseString(tabs.map(_.toString).mkString("\n"))
            }).handler(
              Planify(Debug.path)).run { s =>
                println("server started @ %s" format s.url)
                lat.map(_.await())                
              }
          })
      case _ => 
        System.err.println("usage: [debugurl]")
    }
    shutdown
  }
  def shutdown = Http.shutdown()
}
