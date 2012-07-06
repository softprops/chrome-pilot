organization := "me.lessis"

name := "chrome-pilot-script"

version  := "0.1.0-SNAPSHOT"

description := "man chrome from the cmd line"

resolvers += Classpaths.typesafeResolver

scalaVersion := "2.9.2"

libraryDependencies <+= (sbtVersion)(
  "org.scala-sbt" %
   "launcher-interface" %
    _ % "provided")

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.2"
