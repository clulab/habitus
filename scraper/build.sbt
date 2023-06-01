
name := "habitus-scraper"

resolvers += "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"

libraryDependencies ++= {
  Seq(
    // This should be a transitive dependency.
//    "org.jsoup"         % "jsoup"           % "1.16.1",
    "org.clulab"       %% "processors-main" % "8.5.3",
    "net.ruippeixotog" %% "scala-scraper"   % "3.1.0"
  )
}
