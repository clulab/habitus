name := "habitus"
organization := "org.clulab"
scalaVersion := "2.12.10"

lazy val core: Project = (project in file("."))
    .enablePlugins(JavaAppPackaging, DockerPlugin)

pomIncludeRepository := { (repo: MavenRepository) =>
  repo.root.startsWith("http://artifactory.cs.arizona.edu")
}

// for processors-models
resolvers += "Artifactory" at "http://artifactory.cs.arizona.edu:8081/artifactory/sbt-release"

libraryDependencies ++= {
  val procVer = "8.4.7-SNAPSHOT"

  Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "ai.lum" %% "common" % "0.1.5",

    "ai.lum" %% "odinson-core" % "0.4.0",

    "org.clulab"          %%  "processors-main"          % procVer,
    "org.clulab"          %%  "processors-openie"        % procVer,
    "org.clulab"          %%  "processors-corenlp"       % procVer
  )
}

addCommandAlias("dockerize", ";compile;test;docker:publishLocal")
