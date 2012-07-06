package chrome

import dispatch._
import tubesocks._

import net.liftweb.json.JArray

import unfiltered.netty.{ Http => NHttp, _ }
import unfiltered.netty.cycle.Planify
import unfiltered.request._
import unfiltered.response._

object Server {
  sealed trait Status {
    def message: String
  }
  case class Started(url: String) extends Status {
    def message = url
  }
  case class FailedStart(val message: String) extends Status
  case object Usage extends Status {
    def message = "usage: start [debugurl]"
  }

  object InfoFile {
    import java.io.{ File, FileWriter }
    val base = System.getProperty("user.home")
    val name = ".chromep"
    def file = new File(base, name)
    def write(url: String) = {
      val f = file
      if (!f.getParentFile().exists()) f.mkdirs()
      if (!f.exists) f.createNewFile()
      val w = new FileWriter(f)
      w.write(url)
      w.flush()
      w.close()
    }
    def delete =
      file.delete()
  }

  def main(args: Array[String]) {
    run(args) match {
      case Started(url) =>
        println("started proxy on %s" format url)
      case FailedStart(msg) =>
        System.err.println("error starting proxy: %s" format msg)
      case Usage =>
        System.err.println(Usage.message)
    }
  }

  def run(args: Array[String]): Status = {
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
            Server.shutdown
            FailedStart("failed to resolve chrome debug info %s" format err.getMessage)
          }, { tabs =>
            println("communicating with %d chrome tabs" format tabs.size)          
            srvc.handler(Planify{
              case Path(Seg("tldr" :: Nil)) =>
                import TerminalDisplay._
                ResponseString(tabs.map(t => Show.apply(t)).mkString("\n\n"))
            }).handler(Planify(Debug.path))
              .beforeStop {
                println("shutting down %s connections" format tabs.filter(_.socket.open).size)
                tabs.foreach(_.socket.close)
                println("shutting down dispatch")
                Server.shutdown
              }
              .start
              InfoFile.write(srvc.url)
              Started(srvc.url)
          })
      case _ =>
        Usage
    }
  }
  def shutdown = {
    Http.shutdown()
    InfoFile.delete
  }
}
