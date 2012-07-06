package chrome

import net.liftweb.json._
import JsonDSL._

object Ids {
  import java.util.concurrent.atomic.AtomicInteger
  private lazy val cntr = new AtomicInteger()
  def next = cntr.incrementAndGet
  def current = cntr.get
}

object Methods {
  def serialize(m: Method): String =
    compact(render(m match {
      case pm: ParameterizedMethod =>
        ("id" -> pm.id) ~ ("method" -> pm.method) ~ ("params" -> pm.params)
      case m =>
        ("id" -> m.id) ~ ("method" -> m.method)
    }))
}

trait Method {
  def id: Int
  def method: String
}

trait ParameterizedMethod extends Method {
  def params: JValue
}

trait Response {
  def id: Int
  def error: JObject
  def result: JObject
}

object Page {
  def ns(cmd: String) = "Page.%s" format cmd  
  def enable = new Method {
    val id = Ids.next 
    val method = ns("enable")
  }
  def disable = new Method {
    val id = Ids.next
    val method = ns("disable")
  }
  def navigate(to: String) = new ParameterizedMethod {
    val id = Ids.next
    val method = ns("navigate")
    val params: JValue = Map("url" -> to) 
  }
  def reload(ignoreCache: Boolean = true, script: Option[String]) =
    new ParameterizedMethod {
      val id = Ids.next
      val method = ns("reload")
      val params = ("ignoreCache" -> ignoreCache) ~
                    ("scriptToEvaluateOnLoad" -> script)
    }
  def domContentEventFired =
    new ParameterizedMethod {
      val id = Ids.next
      val method = ns("domContentEventFired")
      val params: JValue = ("timestamp" -> System.currentTimeMillis)
    }
  def loadEventFired =
     new ParameterizedMethod {
      val id = Ids.next
      val method = ns("loadEventFired")
      val params: JValue = ("timestamp" -> System.currentTimeMillis)
    }
}
