
name := "habitus-elasticsearch"

resolvers += "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"

libraryDependencies ++= {
  Seq(
    "co.elastic.clients"         % "elasticsearch-java" % "8.12.0",
    "com.fasterxml.jackson.core" % "jackson-databind"   % "2.16.1"
  )
}

