name := "habitus"
organization := "org.clulab"
scalaVersion := "2.12.15"

lazy val core: Project = (project in file("."))
    .enablePlugins(JavaAppPackaging, DockerPlugin)

pomIncludeRepository := { (repo: MavenRepository) =>
  repo.root.startsWith("https://artifactory.clulab.org")
}

// for processors-models
resolvers += "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"
// for ontologies related to eidos
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= {
  val procVer = "8.5.3"

  Seq(
    "ai.lum"                %% "odinson-core"        % "0.4.0",

    "org.clulab"            %% "eidos"               % "1.7.0",
    "org.clulab"            %% "processors-corenlp"  % procVer,
    "org.clulab"            %% "processors-main"     % procVer,
    "org.clulab"            %% "processors-openie"   % procVer,

    "io.github.zamblauskas" %% "scala-csv-parser"    % "0.13.1",

    "org.scalatest"         %% "scalatest"           % "3.0.5" % "test"
  )
}

addCommandAlias("dockerize", ";compile;test;docker:publishLocal")

// This with true seems to help with Ctrl+C processing.
// run / fork := true
// However, it has the side-effect of causing the BeliefShell to fall through and exit. 
run / fork := false
