val scala211 = "2.11.12" // up to 2.11.12
val scala212 = "2.12.18" // up to 2.12.18
val scala213 = "2.13.11" // up to 2.13.11
val scala30  = "3.0.2"   // up to 3.0.2
val scala31  = "3.1.3"   // up to 3.1.3
val scala32  = "3.2.2"   // up to 3.2.2
val scala33  = "3.3.0"   // up to 3.3.0
val scala3   = scala31

ThisBuild / crossScalaVersions := Seq(scala212, scala211, scala213, scala3)
ThisBuild / scalaVersion := scala212

name := "cluster"

libraryDependencies ++= {
  val breezeVersion = CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, minor)) if minor < 12 => "1.0"
    case _ => "2.1.0"
  }
  val ejmlVersion = "0.41" // Use this older version for Java 8.

  Seq(
    "org.ejml"       % "ejml-core"   % ejmlVersion,
    "org.ejml"       % "ejml-ddense" % ejmlVersion,
    "org.ejml"       % "ejml-simple" % ejmlVersion,
    "org.scalanlp"  %% "breeze"      % breezeVersion,

    "org.scalatest" %% "scalatest"   % "3.2.10" % Test
  )
}

lazy val root = (project in file("."))
