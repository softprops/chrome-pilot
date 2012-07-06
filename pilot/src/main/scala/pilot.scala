package chrome

import xsbti.{ AppMain, AppConfiguration }
import dispatch._

object Pilot {
  import Exiting._
  import Term._
  
  def apply(args: Array[String]): Int = {
    args.toList match {
      case List("start", extras @ _*) =>
        Server.InfoFile.file match {
          case ne if(!ne.exists) =>
            val uri = extras match {
              case List(AnyUrl(uri)) => uri
              case _ => DefaultUri
            }
            ok {
              Server.main(Array(uri))
            }
          case _ =>
            err("%s. server is already running at %s" format(
              red("beep"), serverurl.get))
        }
      case List("tldr") =>
        serving { surl =>
            Http(url(surl) / "tldr" OK As.string)
              .either().fold(err, ok)
        }
      case List("page", extras @ _*) =>
        serving { surl =>
          val page = url(surl).POST / "exec" / "Page"
          extras.toList match {
            case List(AnyUrl(uri)) =>
              Http(page / "navigate" <<? Map("url" -> uri) > As.string)
                .either().fold(err, ok)
            case List("-r", rextras @ _*) =>
              Http(page / "reload" > As.string)
                .either().fold(err, ok)
            case _ =>
              err("usage: page [-r|-u=http://host.com] ")
          }
        }
      case List("net", extras @ _*) =>
        serving { surl =>
          val network = url(surl).POST / "exec" / "Network"
          extras.toList match {
            case List("-e" | "--enable") =>
              Http(network / "enable" > As.string)
                .either().fold(err, ok)
            case List("-d" | "--disable") =>
              Http(network / "disable" > As.string)
                .either().fold(err, ok)
            case List("--clearcache") =>
              Http(network / "clearBrowserCache" > As.string)
                .either().fold(err, ok)
            case List("--clearcookies") =>
              Http(network / "clearBrowserCookies" > As.string)
                .either().fold(err, ok)
            case _ =>
              err("usage: net [-e|--enable|-d|--disable|--clearcache|--clearcookies]")
          }
        }
      case List("timeline", extras @ _*) =>
        serving { surl =>
          val timeline = url(surl).POST / "exec" / "Timeline"
          extras.toList match {
            case List("--start") =>
              Http(timeline / "start" > As.string)
                .either().fold(err, ok)
            case List("--stop") =>
              Http(timeline / "stop" > As.string)
                .either().fold(err, ok)
            case _ =>
              err("usage: timeline [--start|--stop]")
          }
        }
      case List("console", extras @ _*) =>
        serving { surl =>
          val console = url(surl).POST / "exec" / "Console"
          extras.toList match {
            case List("-c" | "--clear") =>
              Http(console / "clearMessages" > As.string)
                .either().fold(err, ok)
            case List("-d" | "--disable") =>
              Http(console / "disable" > As.string)
                .either().fold(err, ok)
            case List("-e" | "--enable") =>
              Http(console / "enable" > As.string)
                .either().fold(err, ok)
            case _ =>
              err("usage: console [-c|--clear|-d|--disable|-e|--enable]")
          }
        }
      case List("docs") =>
        serving { surl =>
          Http(url(surl).POST / "exec" / "Page" / "navigate" <<? Map(
            "url" -> "https://github.com/softprops/chrome-pilot/#readme"
          ) > As.string).either().fold(err, ok)
        }
      case List("issues") =>
         serving { surl =>
          Http(url(surl).POST / "exec" / "Page" / "navigate" <<? Map(
            "url" -> "https://github.com/softprops/chrome-pilot/issues"
          ) > As.string).either().fold(err, ok)
        }
      case _ =>
        err("usage: chromep: [start [-u=http://host.com]|page [-r|-u=http://host.com/]]|net [-d|--disable|-e|--enable|--clearcache|--clearcookies]")
    }
  }

  private val DefaultUri = "http://localhost:9222/json"

  private val AnyUrl = """-u=(.+)""".r

  private lazy val serverurl: Option[String] = Server.InfoFile.file match {
    case ne if (!ne.exists) => None
    case f =>
      io.Source.fromFile(f).getLines().toList.headOption
  }

  private def serving(f: String => Int) =
    serverurl match {
      case Some(surl) =>
        f(surl)
      case _ =>
        err("%s. try the start command" format red("server not started"))
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
