name := "testLiftweb"

version := "0.1"

organization := "zhongshigang"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

Seq(com.earldouglas.xsbtwebplugin.WebPlugin.webSettings: _*)

unmanagedResourceDirectories in Test <+= baseDirectory {
  _ / "src/main/webapp"
}

scalacOptions ++= Seq("-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:postfixOps",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code"
)

libraryDependencies ++= {
  val liftVersion = "3.0-M8"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion,
    "net.liftweb" %% "lift-json-ext" % liftVersion,
    "org.squeryl" %% "squeryl" % "0.9.6-RC4",
    "com.h2database" % "h2" % "1.4.191",
    "ch.qos.logback" % "logback-classic" % "1.1.5",
    "org.eclipse.jetty" % "jetty-webapp" % "9.2.12.v20150709" % "container,test",
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "net.liftweb" %% "lift-testkit" % liftVersion % "test",
    "org.seleniumhq.selenium" % "selenium-java" % "2.52.0" % "test"
  )
}

