name := """En-Taku"""

version := "0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
lazy val core = project

scalaVersion := "2.11.8"

val akkaVersion = "2.4.9"

resolvers += Resolver.jcenterRepo

routesGenerator := InjectedRoutesGenerator

routesImport += "utils.route.Binders._"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.webjars" %% "webjars-play" % "2.5.0-2",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3",
  "org.webjars" % "handlebars" % "4.0.2",
  "org.spire-math" %% "spire" % "0.11.0", // for Mersenne twister
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  evolutions, // for using slick and db
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.5.2", // for using slick and db
  //"mysql" % "mysql-connector-java" % "5.1.39", // for using slick and db
  "com.typesafe.slick" %% "slick" % "3.1.1", // for using slick and db
  "com.typesafe.slick" %% "slick-codegen" % "3.1.1", // for using slick and db
  "com.typesafe.play" %% "play-slick" % "2.0.0", // for using slick and db
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0", // for using slick and db
  "org.scala-lang" % "scala-reflect" % scalaVersion.value, // for using slick and db
  "com.mohiva" %% "play-silhouette" % "4.0.0", // for authentication
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0", // for authentication
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0", // for authentication
  "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0", // for authentication
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x", // for authentication
  "net.codingwell" %% "scala-guice" % "4.0.1", // for authentication
  "com.iheart" %% "ficus" % "1.2.6", // for authentication
  "com.typesafe.play" %% "play-mailer" % "5.0.0", // for authentication
  specs2 % Test,
  cache,
  filters
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

//LessKeys.compress in Assets := true
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

pipelineStages := Seq(digest)

includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"

javaOptions in Test ++= Seq("-Dlogger.resource=logback-test.xml")

enablePlugins(JavaAppPackaging)

fork in run := true

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

//********************************************************
// Scalariform settings
//********************************************************
import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences._

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)

import com.typesafe.config.ConfigFactory

// Slick code generator
slickCodeGen <<= slickCodeGenTask // register sbt command
//(compile in Compile) <<= (compile in Compile) dependsOn (slickCodeGenTask) // register automatic code generation on compile
lazy val config = ConfigFactory.parseFile(new File("./conf/local_settings/db_setting.conf"))
lazy val slickCodeGen = taskKey[Seq[File]]("slick-codegen")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val slickDriver = config.getString("slick.dbs.default.driver").init
  val jdbcDriver = config.getString("slick.dbs.default.db.driver")
  val url = config.getString("slick.dbs.default.db.url")
  val outputDir = "app/"
  val pkg = "models"
  val username = config.getString("slick.dbs.default.db.user")
  val password = config.getString("slick.dbs.default.db.password")

  toError(
    r.run(
      "slick.codegen.SourceCodeGenerator",
      cp.files,
      Array(slickDriver, jdbcDriver, url, outputDir, pkg, username, password),
      s.log
    )
  )
  val fname = outputDir + "/Tables.scala"
  Seq(file(fname))
}
