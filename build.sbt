name := "habitus"
organization := "org.clulab"
scalaVersion := "2.12.10"
libraryDependencies ++= {
  val procVer = "8.2.6"

  Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "ai.lum" %% "common" % "0.1.5",

    "ai.lum" %% "odinson-core" % "0.3.2-SNAPSHOT",

    "org.clulab"          %%  "processors-main"          % procVer,
    "org.clulab"          %%  "processors-corenlp"       % procVer,
    "org.clulab"          %%  "processors-odin"          % procVer
  )
}


