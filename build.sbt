name := "habitus"
organization := "org.clulab"
scalaVersion := "2.12.10"
libraryDependencies ++= {
  val procVer = "8.4.4"

  Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "ai.lum" %% "common" % "0.1.5",

    "ai.lum" %% "odinson-core" % "0.4.0",

    "org.clulab"          %%  "processors-main"          % procVer,
    "org.clulab"          %%  "processors-corenlp"       % procVer
  )
}


