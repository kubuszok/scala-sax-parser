import kubuszok.sbt._
import kubuszok.sbt.KubuszokPlugin.autoImport._
import sbtwelcome.UsefulTask


Global / allowUnsafeScalaLibUpgrade := true
// Versions:

val scala3 = "3.3.5"
val scala213 = "2.13.18"
val scalaXmlVersion = "2.3.0"
val munitVersion = "1.0.4"

val scalas = List(scala213, scala3)

// Common settings:

lazy val commonJsNativeSettings = Seq(
  Compile / unmanagedSourceDirectories +=
    (Compile / sourceDirectory).value / "scala-jsNative"
)

val publishSettings = Seq(
  organization := "com.kubuszok",
  homepage := Some(url("https://github.com/kubuszok/scala-sax-parser")),
  organizationHomepage := Some(url("https://kubuszok.com")),
  licenses := Seq("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/kubuszok/scala-sax-parser/"),
      "scm:git:git@github.com:kubuszok/scala-sax-parser.git"
    )
  ),
  startYear := Some(2026),
  developers := List(
    Developer("MateuszKubuszok", "Mateusz Kubuszok", "", url("https://kubuszok.com"))
  ),
  pomExtra := (
    <issueManagement>
      <system>GitHub issues</system>
      <url>https://github.com/kubuszok/scala-sax-parser/issues</url>
    </issueManagement>
  ),
  projectType := ProjectType.ScalaLibrary
)

val noPublishSettings =
  Seq(projectType := ProjectType.NonPublished)

// Modules:

lazy val al = new Aliases(
  published = Seq(saxParser, tests)
)

lazy val root = project
  .in(file("."))
  .enablePlugins(KubuszokRootPlugin)
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(
    name := "scala-sax-parser-root",
    logo :=
      s"""scala-sax-parser ${version.value} for ($scala213, $scala3) x (JVM, Scala.js, Scala Native)
         |
         |This build uses sbt-projectmatrix:
         | - Scala JVM adds no suffix to a project name seen in build.sbt
         | - Scala.js adds the "JS" suffix to a project name seen in build.sbt
         | - Scala Native adds the "Native" suffix to a project name seen in build.sbt
         | - Scala 2.13 adds no suffix to a project name seen in build.sbt
         | - Scala 3 adds the suffix "3" to a project name seen in build.sbt
         |""".stripMargin,
    usefulTasks := al.usefulTasks()
  )
  .aggregate(saxParser.projectRefs *)
  .aggregate(tests.projectRefs *)

lazy val saxParser = (projectMatrix in file("sax-parser"))
  .settings(
    name := "scala-sax-parser",
    moduleName := "scala-sax-parser"
  )
  .settings(publishSettings)
  .jvmPlatform(scalaVersions = scalas)
  .jsPlatform(scalaVersions = scalas, settings = commonJsNativeSettings)
  .nativePlatform(scalaVersions = scalas, settings = commonJsNativeSettings)

lazy val tests = (projectMatrix in file("tests"))
  .settings(
    name := "tests",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %%% "scala-xml" % scalaXmlVersion,
      "org.scalameta" %%% "munit" % munitVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .settings(noPublishSettings)
  .jvmPlatform(scalaVersions = List(scala3, scala213))
  .jsPlatform(scalaVersions = List(scala3, scala213), settings = commonJsNativeSettings)
  .nativePlatform(scalaVersions = List(scala3, scala213), settings = commonJsNativeSettings)
  .dependsOn(saxParser)
