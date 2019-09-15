val awsVersion = "2.8.7"

addSbtPlugin("marialivia.ch" % "sbt-local-aws" % "0.1.0-SNAPSHOT")

libraryDependencies ++= Seq(
  "software.amazon.awssdk" % "dynamodb" % awsVersion,
  "software.amazon.awssdk" % "s3" % awsVersion
)