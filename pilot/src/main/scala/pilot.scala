package chrome

import xsbti.{ AppMain, AppConfiguration }
import dispatch.Http

object Pilot {
  def apply(args: Array[String]): Int = {
    args.toList match {
      case List("start", extras @ _*) =>
        val DebugUri = """-u=(.+)""".r
        val uri = extras match {
          case List(DebugUri(uri)) => uri
          case _ => "http://localhost:9222/json"
        }
        // todo. pilot should start a server
        // and write a .pid file which contains its host and port
        Server.main(Array(uri))
        0
      case _ =>
        System.err.println("usage: start [uri]")
        1
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
