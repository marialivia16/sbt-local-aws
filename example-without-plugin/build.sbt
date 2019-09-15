val awsVersion = "2.8.7"

libraryDependencies ++= Seq(
  "software.amazon.awssdk" % "dynamodb" % awsVersion,
  "software.amazon.awssdk" % "s3" % awsVersion
)