name := """En-Taku"""

version := "0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

val akkaVersion = "2.4.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.webjars" %% "webjars-play" % "2.4.0",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" % "jquery" % "1.1.1",
  "org.webjars" % "handlebars" % "4.0.2",
  "org.spire-math" %% "spire" % "0.11.0", // for Mersenne twister
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  evolutions, // for using slick and db
  //"org.mariadb.jdbc" % "mariadb-java-client" % "1.3.3", // for using slick and db
  "mysql" % "mysql-connector-java" % "5.1.39", // for using slick and db
  "com.typesafe.slick" %% "slick" % "3.1.1", // for using slick and db
  "com.typesafe.slick" %% "slick-codegen" % "3.1.1", // for using slick and db
  "com.typesafe.play" %% "play-slick" % "2.0.0", // for using slick and db
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0", // for using slick and db
  "org.scala-lang" % "scala-reflect" % scalaVersion.value, // for using slick and db
  specs2 % Test
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
