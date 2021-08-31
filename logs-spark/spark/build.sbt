name := "Processador de Logs Web"
version := "1.0"
scalaVersion := "2.12.10"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.1" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10_2.12" % "3.1.1" % "provided"
libraryDependencies += "org.apache.hbase" % "hbase-client" % "2.4.5"
libraryDependencies += "org.apache.hbase" % "hbase-common" % "2.4.5"