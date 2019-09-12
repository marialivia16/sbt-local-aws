import sbt._

import sys.process._
import scala.collection.immutable.Seq

object LocalAwsPlugin extends AutoPlugin {
  object autoImport {
    lazy val localAwsStart = TaskKey[Unit]("localAwsStart", "Creates a local AWS stack from provided cloudformation.")
    lazy val localAwsStop = TaskKey[Unit]("localAwsStop", "Stops the local AWS stack.")

    lazy val localAwsCloudformationLocation = settingKey[File]("The location of the cloudformation file.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    localAwsCloudformationLocation := { sys.error("Please provide the path to the cloudformation file.") },

    localAwsStart := {
      //Spin up docker with localstack image and creds
      val dockerCmd =
        "docker run -d -p 4569:4569 -e SERVICES=dynamodb  localstack/localstack" //--e AWS_ACCESS_KEY_ID=foobar --e AWS_SECRET_ACCESS_KEY=foobar --e AWS_DEFAULT_REGION=us-east-1"
      println(dockerCmd)
      dockerCmd.!

      //TODO: remember to run "aws configure"

      //TODO: Can "pip install awscli-local" be installed here instead?
      "brew install awscli".!

      //Parse cf
      val cmds = YMLParser.getSupportedServices(localAwsCloudformationLocation.value)

      //TODO: Execute aws cli commands
      cmds.fold(err => println(s"[ERROR] Parsing yaml returned: $err"), resources => {
        resources.foreach {
          case Right(cmd) =>
            println(cmd)
            cmd.!
          case Left(err) => println(s"[ERROR] Command returned: $err")
        }
      })
    },

    localAwsStop := {
      "docker stop $(docker ps -q --filter ancestor=localstack/localstack)".!
    }
  )
}
