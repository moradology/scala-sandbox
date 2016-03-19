lazy val commonSettings = Seq(
  scalaVersion := Version.scala,
  crossScalaVersions := Version.crossScala,
  description := "Sandbox for Scala development",
  organization := "sandbox",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-Yinline-warnings",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature"),

  resolvers += Resolver.bintrayRepo("azavea", "maven"),
  libraryDependencies ++= Seq(
    "org.scalatest"       %%  "scalatest"      % Version.scalaTest % "test"
  ),

  parallelExecution in Test := false,

  shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
)

lazy val root = Project("root", file(".")).aggregate(scalaSandbox)

lazy val scalaSandbox = Project("sandbox", file("sandbox")).
  settings(commonSettings: _*)

