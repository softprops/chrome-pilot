package chrome

import dispatch._
import tubesocks._

import net.liftweb.json.JArray

import unfiltered.netty.{ Http => NHttp, _ }
import unfiltered.netty.cycle.Planify
import unfiltered.request._
import unfiltered.response._

object Server {
  import Term._
  def main(args: Array[String]) {
    run(args) match {
      case Started(url) =>
        println("started proxy on %s" format url)
      case FailedStart(msg) =>
        System.err.println("%s: %s" format(red("error starting proxy"), bold(msg)))
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
          TabInfo.fromJson(js)
                  .filter(!_.title.startsWith("chrome-extension:"))
                  .map { info =>              
                    Tab(info, Channel.uri(info.wsdebugUrl) {
                      case Open(s) =>
                        println("[%s] %s" format(
                          cyan(info.title), magenta("open")))
                      case Close(s) =>
                        println("[%s] %s" format(
                          cyan(info.title), magenta("closed")))
                        countdown.tick
                      case Message(m) =>
                        println("[%s] %s" format(cyan(info.title), m))
                    })
                 }
          }().fold({ err =>
            Server.shutdown
            FailedStart("failed to resolve chrome debug info %s" format err.getMessage)
          }, { tabs =>
            if (tabs.isEmpty) FailedStart("you have no tabs open")
            else {
              countdown.reset(tabs.size)
              println("%s with %s chrome tabs" format(
                green("communicating"), bold(tabs.size)))
              srvc.handler(Planify{
                case Path(Seg("tldr" :: Nil)) =>
                  import TerminalDisplay._
                  ResponseString(tabs.filter(_.socket.open).map(t => Show.apply(t)).mkString("\n\n"))
                case POST(Path(Seg("exec"  :: cmd :: name :: Nil))) & Params(p) =>
                  val msg = Methods.serialize(cmd, name, p)
                  tabs.filter(_.socket.open).map { t =>
                    t.socket.send(msg)                            
                  }
                  Accepted
              })
              .handler(Planify(Debug.path))
              .beforeStop {
                val sd = green("shutting down")
                println("%s %s connections" format(
                  sd, bold(tabs.filter(_.socket.open).size)))
                tabs.foreach(_.socket.close)
                Server.shutdown
                println("%s safely" format green("landed"))
              }
              .run({ s =>
                InfoFile.write(s.url)
                println("%s chrome pilot" format(green("started")))
              })
              Started(srvc.url)
            }
          })
      case _ =>
        Usage
    }
  }

  def shutdown = {
    Http.shutdown()
    InfoFile.delete
  }

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
}
