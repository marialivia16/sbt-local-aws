name := "sbt-local-aws"

version := "0.1"

scalaVersion := "2.12.8"

sbtPlugin := true

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")


libraryDependencies ++= Seq(
"io.circe" %% "circe-yaml" % "0.9.0",
"com.beachape" %% "enumeratum-circe" % "1.5.19"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
