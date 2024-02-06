import org.clulab.sbt.BuildUtils

name := "habitus"
organization := "org.clulab"
scalaVersion := "2.12.15"

lazy val core: Project = (project in file("."))
    .dependsOn(elasticsearch)
    .enablePlugins(JavaAppPackaging, DockerPlugin)

// NOTE: The scraper requires Java 11+ to run!
lazy val scraper = project

lazy val elasticsearch = project

pomIncludeRepository := { (repo: MavenRepository) =>
  repo.root.startsWith("https://artifactory.clulab.org")
}

// for processors-models
resolvers += "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"
// for ontologies related to eidos
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= {
  val luceneVer = "6.6.6"

  Seq(
    "ai.lum"                %% "odinson-core"        % "0.4.0",

    "io.github.zamblauskas" %% "scala-csv-parser"    % "0.13.1",

    "org.clulab"            %% "eidos"               % BuildUtils.eidosVer,
    "org.clulab"            %% "processors-corenlp"  % BuildUtils.procVer,
    "org.clulab"            %% "processors-main"     % BuildUtils.procVer,
    "org.clulab"            %% "processors-openie"   % BuildUtils.procVer,

    "org.apache.lucene"      % "lucene-core"             % luceneVer,
    "org.apache.lucene"      % "lucene-analyzers-common" % luceneVer,
    "org.apache.lucene"      % "lucene-queryparser"      % luceneVer,

    "org.scalatest"         %% "scalatest"           % "3.0.5" % "test"
  )
}

addCommandAlias("dockerize", ";compile;test;docker:publishLocal")

// This with true seems to help with Ctrl+C processing.
// run / fork := true
// However, it has the side-effect of causing the BeliefShell to fall through and exit. 
run / fork := false
