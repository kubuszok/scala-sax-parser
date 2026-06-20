import commandmatrix.extra._
import kubuszok.sbt._
import kubuszok.sbt.KubuszokPlugin.autoImport._


Global / allowUnsafeScalaLibUpgrade := true
// Versions:

val scala3 = "3.3.8"
val scala213 = "2.13.18"
val scalaXmlVersion = "2.4.0"
val munitVersion = "1.3.3"

val scalas = List(scala213, scala3)
val platforms = List(VirtualAxis.jvm, VirtualAxis.js, VirtualAxis.native)

// Common settings:

lazy val commonJsNativeSettings = Seq(
  Compile / unmanagedSourceDirectories +=
    (Compile / sourceDirectory).value / "scala-jsNative"
)

// Matrix-wide actions applied via sbt-commandmatrix's .someVariations:
//
//  - JDK 26+ future-proofing: on Scala 3.3.x, JVM only, enable -Yfuture-lazy-vals (the JDK-9+
//    lazy-vals encoding). It requires -java-output-version >= 9, so we also pin output to 17.
//    Never applied on Scala 2.13, and never on JS/Native (Scala Native 0.5.12 crashes on this flag).
//  - JS/Native shared sources: add the scala-jsNative source directory for both non-JVM platforms.
lazy val matrixActions = Seq(
  MatrixAction
    .ForPlatform(VirtualAxis.jvm)
    .Configure(
      _.settings(
        scalacOptions ++= {
          if (scalaVersion.value == scala3) Seq("-Yfuture-lazy-vals", "-java-output-version", "17")
          else Seq.empty
        }
      )
    ),
  MatrixAction
    .ForPlatforms(VirtualAxis.js, VirtualAxis.native)
    .Configure(_.settings(commonJsNativeSettings))
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

// CI/test command aliases (e.g. ci-jvm-2_13, test-native-3), consumed by .github/workflows/ci.yml.
// sbt-welcome used to register these via `usefulTasks`, but it has no sbt 2.0 build, so we register
// them directly from the Aliases helper bundled with sbt-kubuszok.
def aliasName(prefix: String, platform: String, scalaBinary: String): String =
  s"$prefix-${platform.toLowerCase}-${scalaBinary.replace('.', '_')}"

// In sbt 2.0 the `test` task is incremental and machine-wide cached (it behaves like sbt 1.x's
// `testQuick`), so a CI run that begins with `clean` can still report "No tests to run" and pass
// vacuously. `testFull` is the uncached full run. Rewrite the generated `<id>/test` steps to
// `<id>/testFull` so CI always executes the whole suite.
def fullTests(command: String): String =
  command
    .split(";")
    .map { step =>
      val trimmed = step.trim
      if (trimmed.endsWith("/test")) trimmed.stripSuffix("/test") + "/testFull" else trimmed
    }
    .mkString(" ; ")

lazy val ciAliases: Seq[Def.Setting[?]] = {
  val platformNames = List("JVM", "JS", "Native")
  val scalaBinaries = List("2.13", "3")
  val perCombination = for {
    platform <- platformNames
    scalaBinary <- scalaBinaries
    setting <-
      addCommandAlias(aliasName("ci", platform, scalaBinary), fullTests(al.ci(platform, scalaBinary))) ++
        addCommandAlias(aliasName("test", platform, scalaBinary), fullTests(al.test(platform, scalaBinary)))
  } yield setting
  perCombination ++ addCommandAlias("ci-release", al.release)
}

lazy val root = project
  .in(file("."))
  .enablePlugins(KubuszokRootPlugin)
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(ciAliases)
  .settings(
    name := "scala-sax-parser-root"
  )
  .aggregate(saxParser.projectRefs *)
  .aggregate(tests.projectRefs *)

lazy val saxParser = (projectMatrix in file("sax-parser"))
  .someVariations(scalas, platforms)(matrixActions *)
  .settings(
    name := "scala-sax-parser",
    moduleName := "scala-sax-parser"
  )
  .settings(publishSettings)

lazy val tests = (projectMatrix in file("tests"))
  .someVariations(scalas, platforms)(matrixActions *)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq(
      // sbt 2.0: %% is platform-aware (encodes Scala version + JS/Native suffix); %%% is gone.
      "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion,
      "org.scalameta" %% "munit" % munitVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .settings(noPublishSettings)
  .dependsOn(saxParser)
