import com.jsuereth.sbtpgp.PgpKeys.publishSigned

// Used to publish snapshots to Maven Central.
val mavenCentralSnapshots = "Maven Central Snapshots" at "https://central.sonatype.com/repository/maven-snapshots"

// Versions:

val scala3 = "3.3.5"
val scala213 = "2.13.16"
val scalaXmlVersion = "2.3.0"
val munitVersion = "1.0.3"

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
    Developer("MateuszKubuszok", "Mateusz Kubuszok", "", url("https://github.com/MateuszKubuszok"))
  ),
  pomExtra := (
    <issueManagement>
      <system>GitHub issues</system>
      <url>https://github.com/kubuszok/scala-sax-parser/issues</url>
    </issueManagement>
  ),
  publishTo := {
    if (isSnapshot.value) Some(mavenCentralSnapshots)
    else sonatypePublishToBundle.value
  },
  sonatypeCredentialHost := "central.sonatype.com",
  credentials ++= {
    for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "central.sonatype.com", username, password)
  }.toSeq,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  // Sonatype ignores isSnapshot setting and only looks at -SNAPSHOT suffix in version:
  //   https://central.sonatype.org/publish/publish-maven/#performing-a-snapshot-deployment
  // meanwhile sbt-git used to set up SNAPSHOT if there were uncommitted changes:
  //   https://github.com/sbt/sbt-git/issues/164
  // (now this suffix is empty by default) so we need to fix it manually.
  git.gitUncommittedChanges := git.gitCurrentTags.value.isEmpty,
  git.uncommittedSignifier := Some("SNAPSHOT")
)

val noPublishSettings =
  Seq(publish / skip := true, publishArtifact := false)

val versionSchemeSettings = Seq(versionScheme := Some("early-semver"))

// Modules:

lazy val root = project
  .in(file("."))
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(
    name := "scala-sax-parser-root",
    git.useGitDescribe := true
  )
  .aggregate(saxParser.projectRefs *)
  .aggregate(tests.projectRefs *)

lazy val saxParser = (projectMatrix in file("sax-parser"))
  .settings(
    name := "scala-sax-parser",
    moduleName := "scala-sax-parser"
  )
  .settings(publishSettings)
  .settings(versionSchemeSettings)
  .jvmPlatform(scalaVersions = scalas)
  .jsPlatform(scalaVersions = scalas,
    settings = commonJsNativeSettings
  )
  .nativePlatform(scalaVersions = scalas,
    settings = commonJsNativeSettings
  )

lazy val tests = (projectMatrix in file("tests"))
  .dependsOn(saxParser)
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
  .jsPlatform(scalaVersions = List(scala3, scala213),
    settings = commonJsNativeSettings
  )
  .nativePlatform(scalaVersions = List(scala3, scala213),
    settings = commonJsNativeSettings
  )

// Command aliases for CI:

addCommandAlias("ci-jvm-2_13", "clean ; saxParser/compile ; tests/compile ; saxParser/test ; tests/test")
addCommandAlias("ci-jvm-3", "clean ; saxParser3/compile ; tests3/compile ; saxParser3/test ; tests3/test")
addCommandAlias("ci-js-2_13", "clean ; saxParserJS/compile ; testsJS/compile ; saxParserJS/test ; testsJS/test")
addCommandAlias("ci-js-3", "clean ; saxParserJS3/compile ; testsJS3/compile ; saxParserJS3/test ; testsJS3/test")
addCommandAlias("ci-native-2_13", "clean ; saxParserNative/compile ; testsNative/compile ; saxParserNative/test ; testsNative/test")
addCommandAlias("ci-native-3", "clean ; saxParserNative3/compile ; testsNative3/compile ; saxParserNative3/test ; testsNative3/test")
// ci-release is called from GitHub Actions; snapshot vs release is determined by
// git tags via sbt-git (git.uncommittedSignifier appends -SNAPSHOT when no tag)
addCommandAlias("ci-release", "publishSigned")
