import sbt._
import sbt.Keys._

object KnightBuild extends Build {
    // Common build settings
    val buildSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq[Setting[_]](
        organization    := "com.monterail",
        version         := "0.1.0-SNAPSHOT",
        scalaVersion    := "2.9.0-1"
    )

    // Dependencies
    val scalaCompiler   = "org.scala-lang" % "scala-compiler"
    val scalaLibrary    = "org.scala-lang" % "scala-library"
    val scalatest       = "org.scalatest" % "scalatest_2.9.0" % "1.6.1"


    // Projects configuration
    lazy val root = Project("knight", file(".")) aggregate(annotation, plugin, tests)

    lazy val annotation = Project("annotation", file("annotation"),
        settings = buildSettings
    )

    lazy val plugin = Project("plugin", file("plugin"),
        settings = buildSettings ++ Seq(
            libraryDependencies <+= scalaVersion(scalaCompiler % _),
            libraryDependencies <+= scalaVersion(scalaLibrary % _)
        )
    ) dependsOn(annotation)

    lazy val tests = Project("tests", file("tests"),
        settings = buildSettings ++ Seq(
            scalacOptions <+= (packagedArtifact in Compile in plugin in packageBin) map { art =>
                "-Xplugin:%s" format art._2.getAbsolutePath
            },
            scalacOptions += "-Xplugin-require:knight",
            // scalacOptions += "-Ydebug",
            // scalacOptions += "-Ybrowse:knight-generator",
            libraryDependencies += scalatest % "test"
        ) ++ inConfig(Test)(Seq(
            test <<= test.dependsOn(packageBin in plugin, clean)
        ))
    ) dependsOn(plugin)
}
