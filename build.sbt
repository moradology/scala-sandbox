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

  libraryDependencies ++= Seq(
    "org.scalatest"               %% "scalatest"         % Version.scalaTest % "test",
    "org.locationtech.geotrellis" %% "geotrellis-raster" % "1.0.0",
    "org.locationtech.geotrellis" %% "geotrellis-spark"  % "1.0.0",
    "io.spray"                    %%  "spray-json"       % "1.3.3"
  ),

  parallelExecution in Test := false,

  shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
)

lazy val root = Project("root", file(".")).aggregate(scalaSandbox)

lazy val scalaSandbox = Project("sandbox", file("sandbox")).
  settings(commonSettings: _*)

