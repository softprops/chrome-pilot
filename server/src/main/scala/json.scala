package chrome

import net.liftweb.json._
import JsonDSL._

object Json {
  val parsed = dispatch.As.string.andThen(JsonParser.parse)
}
