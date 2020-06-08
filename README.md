# sbt-local-aws

[![Download](https://api.bintray.com/packages/marialiviach/sbt-plugins/sbt-local-aws/images/download.svg) ](https://bintray.com/marialiviach/sbt-plugins/sbt-local-aws/_latestVersion)
[![CircleCI](https://circleci.com/gh/marialivia16/sbt-local-aws/tree/master.svg?style=svg)](https://circleci.com/gh/marialivia16/sbt-local-aws/tree/master)

This is an SBT plugin that, given an AWS [CloudFormation](https://aws.amazon.com/cloudformation/) file, spins up a 
local AWS environment running in Docker, and creates the required resources.

Watch motivation and intro to plugin [here](https://www.youtube.com/watch?v=1O3zTuw9SFI&t=3s)

### Prerequisites
- Install [Docker](https://docs.docker.com/get-docker/)
- Install [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)

Before running remember to configure the AWS credentials.
Just run `aws configure` and provide some dummy values. They will be used by the 
[localstack](https://github.com/localstack/localstack) image.

### Importing
To use the plugin, just add it to your `plugins.sbt` file:
```
resolvers += Resolver.bintrayIvyRepo("marialiviach", "sbt-plugins")
addSbtPlugin("marialivia.ch" % "sbt-local-aws" % "0.1.0")
```

### Configuration
In your `build.sbt` add:
```
lazy val exampleWithPlugin = (project in file("example-with-plugin"))
  .enablePlugins(LocalAwsPlugin)
  .settings(
    name := "example-with-plugin",
    localAwsCloudformationLocation := (Compile / resourceDirectory).value / "cf.yml",
    localAwsStackName := "my-test-stack"
  )
```

- `localAwsCloudformationLocation` = path to the cloudformation yml file. 
When parsing the CloudFormation yaml file, the plugin uses default values for any parameter substitution. 
If no default value is found an error will be returned.
- `localAwsStackName` = the name of the stack that will be created.

### Commands
- `localAwsStart` = starts the required services in Docker using the localstack image, and creates the resources 
from the cloudformation file.
- `localAwsStop` = stops the Docker container running localstack.

## Contributing
Contributions to this plugin are welcome!

For pull requests, please follow the guideline in CONTRIBUTING.md

More support to follow.

## License
This version of `sbt-local-aws` is released under the Apache License, Version 2.0 (see LICENSE.txt). 
