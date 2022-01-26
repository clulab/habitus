name := "habitus"
organization := "org.clulab"
scalaVersion := "2.12.15"

lazy val core: Project = (project in file("."))
    .enablePlugins(JavaAppPackaging, DockerPlugin)

pomIncludeRepository := { (repo: MavenRepository) =>
  repo.root.startsWith("http://artifactory.cs.arizona.edu")
}

// for processors-models
resolvers += ("Artifactory" at "http://artifactory.cs.arizona.edu:8081/artifactory/sbt-release").withAllowInsecureProtocol(true)
// for ontologies related to eidos
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= {
  val procVer = "8.4.7"

  Seq(
    "ai.lum"        %% "odinson-core"        % "0.4.0",

    "org.clulab"    %% "processors-main"     % procVer,
    "org.clulab"    %% "processors-openie"   % procVer,
    "org.clulab"    %% "processors-corenlp"  % procVer,

    "org.clulab"    %% "eidos"               % "1.5.1",

    "org.scalatest" %% "scalatest"           % "3.0.5" % "test"
  )
}

addCommandAlias("dockerize", ";compile;test;docker:publishLocal")

// This seems to help with Ctrl+C processing.
run / fork := true
