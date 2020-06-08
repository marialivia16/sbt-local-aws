scalaVersion := "2.12.10"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

val awsVersion = "2.8.7"

ThisBuild / organization := "marialivia.ch"
ThisBuild / description := "SBT Plugin to spin up localstack container with AWS resources."

ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-local-aws",
    sbtPlugin := true,
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    publishMavenStyle := false,
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-yaml" % "0.9.0",
      "com.beachape" %% "enumeratum-circe" % "1.5.19",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test"
    ))

lazy val exampleWithoutPlugin = (project in file("example-without-plugin"))
  .enablePlugins(DockerComposePlugin)
  .settings(
    name := "example-without-plugin",
    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "dynamodb" % awsVersion,
      "software.amazon.awssdk" % "s3" % awsVersion
    )
  )

lazy val exampleWithPlugin = (project in file("example-with-plugin"))
  .enablePlugins(LocalAwsPlugin)
  .settings(
    name := "example-with-plugin",
    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "dynamodb" % awsVersion,
      "software.amazon.awssdk" % "s3" % awsVersion
    ))
  .settings(
    localAwsCloudformationLocation := (Compile / resourceDirectory).value / "cf.yml",
    localAwsStackName := "my-test-stack")