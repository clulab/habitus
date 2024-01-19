
import org.clulab.sbt.BuildUtils

name := "habitus-scraper"

resolvers += "clulab" at "https://artifactory.clulab.org/artifactory/sbt-release"

libraryDependencies ++= {
  Seq(
    "com.lihaoyi"                   %% "os-lib"            % "0.9.1",
    "com.softwaremill.sttp.client4" %% "core"              % "4.0.0-M6",
    "org.clulab"                    %% "eidos-eidoscommon" % BuildUtils.eidosVer,
    "org.clulab"                    %% "pdf2txt"           % BuildUtils.pdf2txtVer,
    "org.clulab"                    %% "processors-main"   % BuildUtils.procVer,
    "net.ruippeixotog"              %% "scala-scraper"     % "3.1.0",
    "org.scalatest"                 %% "scalatest"         % "3.0.5" % "test"
  )
}

libraryDependencySchemes += "com.lihaoyi" %% "geny" % VersionScheme.Always
