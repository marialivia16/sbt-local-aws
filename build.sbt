scalaVersion := "2.12.8"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")


libraryDependencies ++= Seq(
  "io.circe" %% "circe-yaml" % "0.9.0",
  "com.beachape" %% "enumeratum-circe" % "1.5.19",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-local-aws",
    organization := "marialivia.ch",
    sbtPlugin := true,
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )

lazy val exampleWithoutPlugin = (project in file("example-without-plugin"))
  .enablePlugins(DockerComposePlugin)
  .settings(
    name := "example-without-plugin"
  )

lazy val exampleWithPlugin = (project in file("example-with-plugin"))
  .enablePlugins(LocalAwsPlugin)
  .settings(
    name := "example-with-plugin",
    localAwsCloudformationLocation := (Compile / resourceDirectory).value / "cf.yml",
    localAwsServices := List("dynamodb")
  )