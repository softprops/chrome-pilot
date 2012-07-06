package chrome

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class TabInfo(frontEndUrl: String,
                      faviconUrl: String,
                      thumbnail: String,
                      title: String,
                      url: String,
                      wsdebugUrl: String)

object TabInfo {
  def fromJson(js: JValue) = for {
    JObject(fields) <- js
    JField("devtoolsFrontendUrl", JString(feurl)) <- fields
    JField("faviconUrl", JString(favurl)) <- fields
    JField("thumbnailUrl", JString(thumburl)) <- fields
    JField("title",  JString(title)) <- fields
    JField("url",  JString(url)) <- fields
    JField("webSocketDebuggerUrl", JString(wsurl)) <- fields
  } yield {
    TabInfo(feurl, favurl, thumburl, title, url, wsurl)
  }
}
