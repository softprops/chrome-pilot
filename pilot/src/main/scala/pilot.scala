package chrome

import xsbti.{ AppMain, AppConfiguration }
import dispatch._

object Pilot {
  import Exiting._

  lazy val serverurl: Option[String] = Server.InfoFile.file match {
    case ne if (!ne.exists) => None
    case f =>
      io.Source.fromFile(f).getLines().toList.headOption
  }

  def apply(args: Array[String]): Int = {
    args.toList match {
      case List("start", extras @ _*) =>
        Server.InfoFile.file match {
          case ne if(!ne.exists) =>
            val DebugUri = """-u=(.+)""".r
            val uri = extras match {
              case List(DebugUri(uri)) => uri
              case _ => "http://localhost:9222/json"
            }
            ok {
              Server.main(Array(uri))
            }
          case _ =>
            err("server is already running at %s" format serverurl)
        }
      case List("tldr", extras @ _*) =>
        serverurl match {
          case Some(surl) =>
            Http(url(surl) / "tldr" OK As.string)
              .either().fold(err, ok)
          case _ => err("server not started. try the start command")
        }
      case _ =>
        err("usage: start [uri]")
    }
  }
}

object Main {
  def main(args: Array[String]) {
    System.exit(Pilot(args))
  }
}

class Script extends AppMain {
  def run(conf: AppConfiguration) =
    new Exit(Pilot(conf.arguments))
}

class Exit(val code: Int) extends xsbti.Exit
