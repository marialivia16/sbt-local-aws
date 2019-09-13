import sbt._

import sys.process._
import scala.collection.immutable.Seq
import YMLParser.Command

object LocalAwsPlugin extends AutoPlugin {
  object autoImport {
    lazy val localAwsStart = TaskKey[Unit]("localAwsStart", "Creates a local AWS stack from provided cloudformation.")
    lazy val localAwsStop = TaskKey[Unit]("localAwsStop", "Stops the local AWS stack.")
    lazy val localAwsCliCommands = TaskKey[Unit]("localAwsStop", "Stops the local AWS stack.")

    lazy val localAwsCloudformationLocation = settingKey[File]("The location of the cloudformation file.")
    lazy val localAwsServices = settingKey[List[String]]("The list of services to spin up.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    localAwsCloudformationLocation := { sys.error("Please provide the path to the cloudformation file.") },
    localAwsServices := { sys.error("Please provide the services you need to spin up.") },

    localAwsStart := {
      //Spin up docker for localstack image with required services
      s"docker run -d -p 4569:4569 -e SERVICES=${localAwsServices.value.mkString(",")}  localstack/localstack".!

      //Parse cf
      val cmds: List[Command] = YMLParser.getSupportedServices(localAwsCloudformationLocation.value)

      //Execute aws cli commands or print list of errors
      cmds.foreach {
        case Right(cmd) => cmd.!
        case Left(err) => err.map(println)
      }
    },

    localAwsCliCommands := {
      YMLParser.getSupportedServices(localAwsCloudformationLocation.value).foreach {
        case Right(cmd) => println(s"$cmd\n")
        case Left(err) => err.map(println)
      }
    },

    localAwsStop := {
      "docker stop $(docker ps -q --filter ancestor=localstack/localstack)".!
    }
  )
}
