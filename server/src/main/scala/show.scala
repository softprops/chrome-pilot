package chrome

trait Show[T] {
  def show(t: T): String
}

object Show {
  implicit object ShowTab extends Show[Tab] {
    def show(t: Tab): String = t.info.toString
  }
  def apply[T: Show](t: T) = implicitly[Show[T]].show(t)
}


object TerminalDisplay {
  implicit object ShowTab extends Show[Tab] {
    def show(t: Tab): String =
      """[%s]
      |url: %s
      |front-end url: %s
      |favicon url: %s
      |thumbnail url: %s
      |wsdebug url: %s""".stripMargin.format(
        t.info.title,
        t.info.url,
        t.info.frontEndUrl,
        t.info.faviconUrl,
        t.info.thumbnail,
        t.info.wsdebugUrl
      )
  }
}
