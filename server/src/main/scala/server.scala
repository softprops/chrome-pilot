package chrome

import dispatch._
import tubesocks._

import net.liftweb.json.JArray

import unfiltered.netty.{ Http => NHttp, _ }
import unfiltered.netty.cycle.Planify
import unfiltered.request._
import unfiltered.response._

object Server {
  def main(args: Array[String]) {
    args.toList match {
      case List(uri) =>
        val srvc = NHttp.anylocal
        val countdown = new Countdown(0)({ srvc.stop() })
        Http(url(uri) OK Json.parsed).either.right.map { js =>
          js match {
            case JArray(ary) =>
              countdown.reset(ary.size)
            case _ => 
              System.err.println("expected json array from chrome")
          }
          TabInfo.fromJson(js)
                  .filter(!_.title.startsWith("chrome-extension:"))
                  .map { info =>              
                    Tab(info, Channel.uri(info.wsdebugUrl) {
                      case Open(s) =>
                        println("[%s] open" format info.title)
                      case Close(s) =>
                        println("[%s] closed" format info.title)
                        countdown.tick
                      case Message(m) =>
                        println("[%s] %s" format(info.title, m))
                    })
                 }
          }().fold({ err =>
            System.err.println("failed to resolve chrome debug info %s" format err.getMessage)
            Server.shutdown
          }, { tabs =>
            println("communicating with %d chrome tabs" format tabs.size)          
            srvc.handler(Planify{
              case Path(Seg("tldr" :: Nil)) =>
                import Show._
                ResponseString(tabs.map(t => Show.apply(t)).mkString("\n"))
            }).handler(Planify(Debug.path))
              .run({ s => () }, { s =>
                println("shutting down %s connections" format tabs.filter(_.socket.open).size)
                tabs.foreach(_.socket.close)
                println("shutting down dispatch")
                Server.shutdown
              })
          })
      case _ =>
        System.err.println("usage: [debugurl]")
    }
  }
  def shutdown = Http.shutdown()
}
