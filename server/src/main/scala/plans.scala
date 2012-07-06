package chrome

import unfiltered._
import request._
import response._

object Debug {
  def path: Cycle.Intent[Any, Any] = {
    case Path(p) =>
      println("path %s" format p)
      Ok
  }
}
