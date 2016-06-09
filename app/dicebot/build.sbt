name := """DiceBot"""

version := "0.0-SNAPSHOT"

lazy val dicebot = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0",
  "org.spire-math" %% "spire" % "0.11.0"  // for Mersenne twister
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

javaOptions in Test ++= Seq("-Dlogger.resource=logback-test.xml")

enablePlugins(JavaAppPackaging)

fork in run := true