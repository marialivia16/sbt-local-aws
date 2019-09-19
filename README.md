### sbt-local-aws

[![Download](https://api.bintray.com/packages/marialiviach/sbt-plugins/sbt-local-aws/images/download.svg) ](https://bintray.com/marialiviach/sbt-plugins/sbt-local-aws/_latestVersion)
[![CircleCI](https://circleci.com/gh/marialivia16/sbt-local-aws/tree/master.svg?style=svg)](https://circleci.com/gh/marialivia16/sbt-local-aws/tree/master)

#### Prerequisites
- Install Docker
- Install AWS CLI 

Before running remember to configure the AWS credentials. 
Just run `aws configure` and provide some dummy values. They will be used by localstack.

#### Importing
To use the plugin, just add it to your `plugins.sbt` file:
```
resolvers += Resolver.bintrayIvyRepo("marialiviach", "sbt-plugins")
addSbtPlugin("marialivia.ch" % "sbt-local-aws" % "0.1.0")
```

#### Configuration
In your `build.sbt` add:
```
lazy val exampleWithPlugin = (project in file("example-with-plugin"))
  .enablePlugins(LocalAwsPlugin)
  .settings(
    name := "example-with-plugin",
    localAwsCloudformationLocation := (Compile / resourceDirectory).value / "cf.yml",
    localAwsServices := List("dynamodb")
  )
```

- `localAwsCloudformationLocation`: path to the cloudformation yml file
- `localAwsServices`: list of AWS services you need the plugin to spin up

#### Commands
- `localAwsStart`: starts the specified services in Docker using the localstack image, and creates the resources using the cloudformation file.
- `localAwsStop`: stops the Docker container running localstack.
- `localAwsCommands`: prints out the AWS CLI commands created using the cloudformation.