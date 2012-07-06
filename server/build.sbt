organization := "me.lessis"

name := "chrome-pilot"

version := "0.1.0-SNAPSHOT"

description := "an proxy for issueing requests to an instance of chrome"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "core" % "0.9.0-beta1",
  "net.databinder" %% "unfiltered-netty-server" % "0.6.3",
  "net.liftweb" % "lift-json_2.9.1" % "2.4"
)

seq(assemblySettings: _*)

//jarName in assembly := "chromep-server.jar"
