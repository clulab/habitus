
name := "habitus-sql"

libraryDependencies ++= {
  Seq(
    "mysql"                   % "mysql-connector-java"    % "8.0.33",
    "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0"
  )
}
