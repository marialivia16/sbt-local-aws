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


#### Commands
- localAwsStart
- localAwsStop
- localAwsCommands