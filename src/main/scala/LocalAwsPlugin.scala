import sbt._

import sys.process._
import scala.collection.immutable.Seq

object LocalAwsPlugin extends AutoPlugin {
  object autoImport {
    lazy val localAwsStart = TaskKey[Unit]("localAwsStart", "Creates a local AWS stack from provided cloudformation.")
    lazy val localAwsStop = TaskKey[Unit]("localAwsStop", "Stops the local AWS stack.")

    lazy val localAwsCloudformationLocation = settingKey[File]("The location of the cloudformation file.")
    lazy val localAwsServices = settingKey[List[String]]("The list of services to spin up.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    localAwsCloudformationLocation := { sys.error("Please provide the path to the cloudformation file.") },
    localAwsServices := { sys.error("Please provide the services you need to spin up.") },

    localAwsStart := {
      //Spin up docker with localstack image with required services
      s"docker run -d -p 4569:4569 -e SERVICES=${localAwsServices.value.mkString(",")}  localstack/localstack".!

      //TODO: remember to run "aws configure"

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
