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

  def serving(f: String => Int) =
    serverurl match {
      case Some(surl) =>
        f(surl)
      case _ =>
        err("server not started. try the start command")
    }

  val AnyUrl = """-u=(.+)""".r
  val Reload = """(-r)""".r
  def apply(args: Array[String]): Int = {
    args.toList match {
      case List("start", extras @ _*) =>
        Server.InfoFile.file match {
          case ne if(!ne.exists) =>
            val uri = extras match {
              case List(AnyUrl(uri)) => uri
              case _ => "http://localhost:9222/json"
            }
            ok {
              Server.main(Array(uri))
            }
          case _ =>
            err("server is already running at %s" format serverurl.get)
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
            case List(Reload(_), rextras @ _*) =>
              Http(page / "reload" > As.string)
                .either().fold(err, ok)
            case _ =>
              err("usage page [-r|-u=http://host.com] ")
          }
        }
      case List("docs") =>
        serving { surl =>
          Http(url(surl).POST / "exec" / "Page" / "navigate" <<? Map(
            "url" -> "https://github.com/softprops/chrome-pilot/#readme") > As.string)
            .either().fold(err, ok)
        }
      case List("issues") =>
         serving { surl =>
          Http(url(surl).POST / "exec" / "Page" / "navigate" <<? Map(
            "url" -> "https://github.com/softprops/chrome-pilot/issues") > As.string)
            .either().fold(err, ok)
        }
      case _ =>
        err("usage: chromep: [start [-u=http://host.com]|page [-r|-u=http://host.com/]]")
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
