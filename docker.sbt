import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

Docker / defaultLinuxInstallLocation := "/app"
Docker / dockerBaseImage := "openjdk:8"
Docker / dockerPermissionStrategy := DockerPermissionStrategy.None
Docker / dockerUpdateLatest := true
Docker / maintainer := "Keith Alcock <docker@keithalcock.com>"
Docker / version := "0.2.0"
