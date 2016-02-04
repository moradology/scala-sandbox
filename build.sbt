lazy val commonSettings = Seq(
  version := Version.benchmarks,
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
    "com.azavea" %% "scaliper" % "0.5.0-c5566b1" % "test",
    "org.scalatest"       %%  "scalatest"      % Version.scalaTest % "test"
  ),

  parallelExecution in Test := false,

  shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

lazy val root = Project("root", file(".")).
  dependsOn(scalaSandbox)

lazy val scalaSandbox = Project("sandbox", file("sandbox")).
  settings(commonSettings: _*)
