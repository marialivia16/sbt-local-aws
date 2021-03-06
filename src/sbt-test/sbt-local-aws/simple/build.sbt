lazy val root = (project in file("."))
  .enablePlugins(LocalAwsPlugin)
  .settings(
      version := "0.1",
      scalaVersion := "2.12.10",
      assemblyJarName in assembly := "sbt-local-aws.jar",
      localAwsCloudformationLocation := (Compile / resourceDirectory).value / "cf.yml",
      localAwsStackName := "test-stack"
  )