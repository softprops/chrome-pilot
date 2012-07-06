import sbt._
import Keys._

object Build extends sbt.Build {
  lazy val server = Project("server", file("server")) dependsOn(tubesocks)
  lazy val pilot = Project("pilot", file("pilot")) dependsOn(server)
  lazy val tubesocks = uri(
    "git://github.com/softprops/tubesocks#5950b56d1d1ecbb6b7b85b60a6a3731ecdf2b4fc")
}
