val scala3 = "3.3.5"
val scala213 = "2.13.16"
val scalaXmlVersion = "2.3.0"
val munitVersion = "1.0.3"

ThisBuild / organization := "io.github.scalasaxparser"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val commonJsNativeSettings = Seq(
  Compile / unmanagedSourceDirectories +=
    (Compile / sourceDirectory).value / "scala-jsNative"
)

lazy val saxParser = (projectMatrix in file("sax-parser"))
  .settings(
    name := "scala-sax-parser",
  )
  .jvmPlatform(scalaVersions = Seq(scala3, scala213))
  .jsPlatform(scalaVersions = Seq(scala3, scala213),
    settings = commonJsNativeSettings
  )
  .nativePlatform(scalaVersions = Seq(scala3, scala213),
    settings = commonJsNativeSettings
  )

lazy val tests = (projectMatrix in file("tests"))
  .dependsOn(saxParser)
  .settings(
    name := "tests",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %%% "scala-xml" % scalaXmlVersion,
      "org.scalameta" %%% "munit" % munitVersion % Test,
    ),
    testFrameworks += new TestFramework("munit.Framework"),
  )
  .jvmPlatform(scalaVersions = Seq(scala3))
  .jsPlatform(scalaVersions = Seq(scala3),
    settings = commonJsNativeSettings
  )
  .nativePlatform(scalaVersions = Seq(scala3),
    settings = commonJsNativeSettings
  )
