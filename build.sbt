val AkkaVersion = "2.6.10"
val AkkaManagementVersion = "1.0.9"

resolvers += ("custome1" at "http://4thline.org/m2").withAllowInsecureProtocol(true)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
"com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
"com.typesafe.akka" %% "akka-cluster-sharding" % AkkaVersion,
 "org.fourthline.cling" % "cling-core" % "2.1.2",
 "org.fourthline.cling" % "cling-support" % "2.1.2",
"com.typesafe" % "config" % "1.4.1",
 "org.scalafx" %% "scalafx" % "8.0.192-R14",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.5",
"com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
)

fork := false
//Runtime / unmanagedClasspath += file("/home/net3204/")
//Compile / unmanagedClasspath += file("/home/net3204")

