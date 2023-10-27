
name := "habitus-scraper"

resolvers += "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"

libraryDependencies ++= {
  Seq(
    // This should be a transitive dependency.
    "org.clulab"                    %% "processors-main" % "8.5.3",
    "net.ruippeixotog"              %% "scala-scraper"   % "3.1.0",
    // These are used to download from google searches.
    "com.softwaremill.sttp.client4" %% "core"            % "4.0.0-M6",
    "org.scalatest"                 %% "scalatest"       % "3.0.5" % "test"
  )
}
