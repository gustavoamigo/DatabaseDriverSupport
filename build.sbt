name := "DatabaseDriverSupport"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq("org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "mysql"          % "mysql-connector-java" % "5.1.36",
  "org.postgresql" % "postgresql"           % "9.4-1206-jdbc41",
  "com.github.mauricio" %% "db-async-common" % "0.2.18",
  "com.github.mauricio" %% "mysql-async" % "0.2.18",
  "com.github.mauricio" %% "postgresql-async" % "0.2.18"
)

testOptions in Test += Tests.Argument("-oDF")